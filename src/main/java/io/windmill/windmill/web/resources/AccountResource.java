
package io.windmill.windmill.web.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.services.sns.model.MessageAttributeValue;

import io.windmill.windmill.common.WindmillMetadata;
import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.services.AccountService;
import io.windmill.windmill.services.MustacheWriter;
import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.Notification.Platform;
import io.windmill.windmill.services.NotificationService;
import io.windmill.windmill.services.StorageService;
import io.windmill.windmill.services.WindmillService;
import io.windmill.windmill.web.common.FormDataMap;

/**
 * While developing, looks up ~/.aws/credentials for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "io.windmill" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?region=eu-west-1#roles/io.windmill)
 *
 * @author qnoid
 */
@Path("/account")
public class AccountResource {

	private static final Logger LOGGER = Logger.getLogger(AccountResource.class.getName());
	
	public static final Map<Platform, Map<String, MessageAttributeValue>> attributesMap = new HashMap<Platform, Map<String, MessageAttributeValue>>();
	static {
		attributesMap.put(Platform.APNS, null);
		attributesMap.put(Platform.APNS_SANDBOX, null);
	}
	
    @Inject
    private WindmillService windmillService;

    @Inject
    private AccountService accountService;

    @Inject
    private StorageService storageService;

    @Inject
    private NotificationService notificationService;

    @GET
    @Path("/{account}/windmill")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response read(@PathParam("account") final String account_identifier) {

      List<Windmill> windmills = this.windmillService.get(account_identifier);
      
      return Response.ok(windmills).build();      
    }

    /**
     * 
     * @param account_identifier
     * @param input a form with the following parameters, <b>plist</b>, <b>ipa</b> where plist is a 'manifest.xml' file and 'ipa' a binary IPA file as produced by the 'xcodebuild exportArchive' command.
     * @return
     * @see <a href="https://developer.apple.com/legacy/library/documentation/Darwin/Reference/ManPages/man1/xcodebuild.1.html">xcodebuild</a>
     */
    @POST
    @Path("/{account}/windmill")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(@PathParam("account") final String account_identifier, final MultipartFormDataInput input) {
        final FormDataMap formDataMap = FormDataMap.get(input);

    	try {    		
    		WindmillMetadata windmillMetadata = formDataMap.readWindmillMetadata();

    		String windmill_identifier = windmillMetadata.getIdentifier();
	        String windmill_title = windmillMetadata.getTitle();
	        Double windmill_version = windmillMetadata.getVersion();
	        
	        InputStream ipaStream = formDataMap.get("ipa");            
	        File file = windmillMetadata.copy(account_identifier, ipaStream);
	        
	        Windmill windmill = this.accountService.create(account_identifier, windmill_identifier, windmill_title, windmill_version);

	        String relativePathToResource = String.format(StorageService.RELATIVE_PATH_TO_RESOURCE, account_identifier, windmill.getIdentifier(), windmill.getVersion(), windmill.getTitle());
			byte[] written = new MustacheWriter().urlString(new InputStreamReader(formDataMap.get("plist"), Charset.forName("UTF-8")), String.format("https://%s/%s.ipa", StorageService.BUCKET_CANONICAL_NAME, relativePathToResource)).toByteArray();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(written);
			
			String itmsURL = this.storageService.uploadWindmill(new FileInputStream(file), file.length(), inputStream, written.length, windmill, relativePathToResource);
	        String notification = Messages.of("New build", String.format("%s %s (455f6a1) is now available to install.", windmill_title, windmill_version));	        
			this.notificationService.notify(notification, notificationService.createPlatform("e14113c658cd67f35a870433f4218d51233eba0cbdc02c88e80adaad1dcc94c6"));

	        return Response.seeOther(URI.create(itmsURL)).build();
		} catch (ConfigurationException e) {
			LOGGER.warn("Unabled to parse 'plist' passed as input", e.getCause());
			return Response.status(Status.BAD_REQUEST).entity("The 'plist' parameter should be a 'manifest.xml' as referenced at https://developer.apple.com/library/content/documentation/IDEs/Conceptual/AppDistributionGuide/TestingYouriOSApp/TestingYouriOSApp.html").build();
		} catch (IllegalArgumentException e) {
			LOGGER.warn(e.getMessage(), e.getCause());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    }
}
