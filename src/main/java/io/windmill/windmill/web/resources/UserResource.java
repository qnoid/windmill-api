
package io.windmill.windmill.web.resources;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.windmill.windmill.aws.AwsBucketService;
import io.windmill.windmill.persistence.User;
import io.windmill.windmill.persistence.UserDAO;
import io.windmill.windmill.web.StringMustache;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * While developing, looks up ~/.aws/config for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "i-b28874f2" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?#roles/i-b28874f2)
 *
 * @author qnoid
 */
@Path("/user")
public class UserResource {

    static final String BUCKET_CANONICAL_NAME = "ota.windmill.io";
    private static final String RELATIVE_PATH_TO_RESOURCE = "%s/%s/%s";


    @Inject
    private AwsBucketService bucket;

    @Inject
    private UserDAO userDAO;

    @POST
    @Path("/{user}/widmill")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("user") final String user, @HeaderParam("binary-length") final long bLength, final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<User> windmills = userDAO.windmills(user);

        System.out.println(windmills);

        try {
            final InputStream ipaStream = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("ipa").get(0));
            final InputStream plistSteam = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0));

            XMLPropertyListConfiguration foo = new XMLPropertyListConfiguration();
            foo.read(new InputStreamReader(plistSteam, Charset.forName("UTF-8")));

            XMLPropertyListConfiguration items = foo.get(XMLPropertyListConfiguration.class, "items");

            String identifier = items.getString("metadata.bundle-identifier");
            String name = items.getString("metadata.title");

            final String relativePathToResource = String.format(RELATIVE_PATH_TO_RESOURCE, user, identifier, name);
            final String itmsURL = String.format("itms-services://?action=download-manifest&url=https://%s/%s.plist", BUCKET_CANONICAL_NAME, relativePathToResource);

            System.out.println(itmsURL);
            final String plist = IOUtils.toString(FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0)), "UTF-8");
            System.out.println(plist);

            final HashMap<String, Object> substitutions = new HashMap<>();
            substitutions.put("URL", String.format("https://%s/%s.ipa", BUCKET_CANONICAL_NAME, relativePathToResource));

            final ByteArrayOutputStream out = PLIST_MUSTACHE.parse(plist, substitutions);

            final ObjectMetadata om = new ObjectMetadata();
            om.setContentLength(bLength);
            bucket.upload(ipaStream, String.format("%s.ipa", relativePathToResource), om);
           // bucket.upload(new ByteArrayInputStream(out.toByteArray()), String.format("%s.plist", relativePathToResource));


            return Response.seeOther(URI.create(itmsURL)).build();
        } catch (final Exception e) {
            e.printStackTrace();
            return Response.status(503).build();
        }
    }



    private static final Function<InputPart, InputStream> FUNCTION_INPUTPART_TO_INPUTSTREAM = (final InputPart inputPart) -> {
        try {
            return inputPart.getBody(InputStream.class, null);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    };

    private static final StringMustache PLIST_MUSTACHE = (final String string, final Map<String, Object> scopes) -> {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(out);
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache mustache = mf.compile(new StringReader(string), "plist");
        mustache.execute(writer, scopes);
        writer.flush();

        return out;
    };
}
