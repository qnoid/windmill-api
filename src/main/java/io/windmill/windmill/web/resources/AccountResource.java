
package io.windmill.windmill.web.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.services.sns.model.MessageAttributeValue;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.AccountDAO;
import io.windmill.windmill.persistence.Action;
import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.persistence.WindmillDAO;
import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.Notification.Platform;
import io.windmill.windmill.services.NotificationService;
import io.windmill.windmill.services.StorageService;

/**
 * While developing, looks up ~/.aws/credentials for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "io.windmill" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?region=eu-west-1#roles/io.windmill)
 *
 * @author qnoid
 */
@Path("/account")
public class AccountResource {

	public static final Map<Platform, Map<String, MessageAttributeValue>> attributesMap = new HashMap<Platform, Map<String, MessageAttributeValue>>();
	static {
		attributesMap.put(Platform.APNS, null);
		attributesMap.put(Platform.APNS_SANDBOX, null);
	}
	
    private static final Logger LOGGER = Logger.getLogger(AccountResource.class.getName());

    @Inject
    private StorageService storageService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AccountDAO accountDAO;
    
    @Inject
    private WindmillDAO windmillDAO;

    @GET
    @Path("/{account}/windmill")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response read(@PathParam("account") final String account_identifier) {

      List<Windmill> windmills = this.windmillDAO.windmills(account_identifier);
      
      LOGGER.info(String.format("Found: %s", windmills.size()));
      
      return Response.ok(windmills).build();      
    }

    @POST
    @Path("/{account}/windmill")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(@PathParam("account") final String account_identifier, @HeaderParam("binary-length") final long bLength, final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        try {
        	final InputStream plistSteam = StorageService.FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0));
        	
            XMLPropertyListConfiguration xmlPropertyListConfiguration = new XMLPropertyListConfiguration();
            xmlPropertyListConfiguration.read(new InputStreamReader(plistSteam, Charset.forName("UTF-8")));

            XMLPropertyListConfiguration items = xmlPropertyListConfiguration.get(XMLPropertyListConfiguration.class, "items");

            String windmill_identifier = items.getString("metadata.bundle-identifier");
            String windmill_title = items.getString("metadata.title");
            Double windmill_version = items.getDouble("metadata.bundle-version");
            
            final InputStream ipaStream = uploadForm.get("ipa").get(0).getBody(InputStream.class, null);            
            File file = foo(account_identifier, windmill_identifier, windmill_title, windmill_version, "%s.ipa");
			FileOutputStream fileOutputStream = new FileOutputStream(file, false);

            writeFile(ipaStream, fileOutputStream);
            
            final InputStream plistStream = uploadForm.get("plist").get(0).getBody(InputStream.class, null);            
            File plistFile = foo(account_identifier, windmill_identifier, windmill_title, windmill_version, "%s.plist");
			FileOutputStream plistFileOutputStream = new FileOutputStream(plistFile, false);

            writeFile(plistStream, plistFileOutputStream);

            Windmill windmill = this.windmillDAO.findOrCreate(windmill_identifier, new Action<Windmill>() {
				
				@Override
				public Windmill create(String identifier) {
					return new Windmill(windmill_identifier, windmill_version, windmill_title);
				}
			});
            
            windmill.setUpdatedAt(Instant.now());
            
            Account account = this.accountDAO.findOrCreate(account_identifier);
            
            LOGGER.info(String.format("Found: %s", account.getIdentifier()));            
            
            account.add(windmill);
            
            this.accountDAO.saveOrUpdate(account);
            
            final String itmsURL = storageService.uploadWindmill(account_identifier, new FileInputStream(file), bLength, new FileInputStream(plistFile), plistFile.length(), windmill);

            String notification = Messages.of("New build", String.format("%s %s (455f6a1) is now available to install.", windmill_title, windmill_version));
            
			notificationService.notify(notification, notificationService.createPlatform("e14113c658cd67f35a870433f4218d51233eba0cbdc02c88e80adaad1dcc94c6"));

            return Response.seeOther(URI.create(itmsURL)).build();
        } catch (final Exception e) {
            e.printStackTrace();
            return Response.status(503).build();
        }
    }

	private File foo(final String account_identifier, String windmill_identifier, String windmill_title,
			Double windmill_version, String filename) throws IOException {
		java.nio.file.Path path = Paths.get("/tmp", account_identifier, windmill_identifier, String.valueOf(windmill_version));
		Files.createDirectories(path);
		File file = new File(path.toFile(), String.format(filename, windmill_title));
		return file;
	}

	private void writeFile(InputStream filecontent, FileOutputStream out) throws IOException {
        int read = 0;
        final byte[] bytes = new byte[1024];

        while ((read = filecontent.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
	}
}
