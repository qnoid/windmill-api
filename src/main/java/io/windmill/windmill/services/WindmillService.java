package io.windmill.windmill.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.jboss.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sns.model.EndpointDisabledException;

import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.sns.Endpoint;
import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.web.common.UriBuilders;

@ApplicationScoped
public class WindmillService {

    private static final Logger LOGGER = Logger.getLogger(WindmillService.class);

    @Inject
    AccountService accountService; 

    @Inject
    NotificationService notificationService; 

    @Inject 
    StorageService storageService;
    
    @Inject
    private WindmillEntityManager entityManager;

    @PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public List<Export> get(UUID account_identifier) {
		
		List<Export> exports = entityManager.getResultList("export.with_account_identifier", query -> query.setParameter("account_identifier", account_identifier));
      
		LOGGER.debug(String.format("Found: %s", exports.size()));
	      
	    return exports;
	}

	/**
	 * 
	 * @param account_identifier
	 * @param manifest
	 * @param ipa
	 * @param plist
	 * @return a URI in the itms format, e.g. "itms-services://?action=download-manifest&url=https://ota.windmill.io/14810686-4690-4900-ADA5-8B0B7338AA39/io.windmill.windmill/1.0/windmill.plist"
	 * @throws FileNotFoundException
	 * @throws URISyntaxException 
	 * @throws UriBuilderException 
	 * @throws IllegalArgumentException 
	 */
	public URI updateOrCreate(UUID account_identifier, Manifest manifest, File ipa, ByteArrayOutputStream plist) throws FileNotFoundException, IllegalArgumentException, URISyntaxException 
	{
		Export export = accountService.updateOrCreate(account_identifier, manifest.getIdentifier(), manifest.getTitle(), manifest.getVersion());
	
	    LOGGER.debug(String.format("Created or updated windmill for %s with metadata.bundle-identifier '%s', metadata.bundle-version '%s', metadata.title '%s'", account_identifier, export.getIdentifier(), export.getVersion(), export.getTitle()));

		URI path = UriBuilder.fromPath(account_identifier.toString())
				.path(manifest.getIdentifier())
				.path(String.valueOf(manifest.getVersion()))
				.path(manifest.getTitle())
				.build();		
		
		try {
			ObjectMetadata ipaMetadata = new ObjectMetadata();
			ipaMetadata.setContentLength(ipa.length());
			storageService.upload(new FileInputStream(ipa), String.format("%s.ipa", path), ipaMetadata);
			
			byte[] byteArray = plist.toByteArray();
			ObjectMetadata plistMetadata = new ObjectMetadata();
			plistMetadata.setContentLength(byteArray.length);			
			storageService.upload(new ByteArrayInputStream(byteArray), String.format("%s.plist", path), plistMetadata);			
		}
		catch (AmazonClientException | InterruptedException amazonException) {
			LOGGER.error("Unable to upload file, upload aws aborted.", amazonException);
			throw new StorageServiceException(amazonException);
		}
		
		 String notification = Messages.of("New build", String.format("%s %s is now available to install.", export.getTitle(), export.getVersion()));
        
		List<Endpoint> endpoints = entityManager.getResultList("endpoint.find_by_account_identifier", query -> query.setParameter("account_identifier", account_identifier)); 

		for (Endpoint endpoint : endpoints) {
			String endpointArn = endpoint.getArn();
			
			try {
				boolean success = notificationService.notify(notification, endpointArn);
				if (success) {
					LOGGER.info(String.format("Succesfully sent notification to endpoint '%s' for account '%s'.", endpoint, account_identifier));
				}
			}
			catch (EndpointDisabledException e) {
				LOGGER.debug(String.format("Endpoint `%s` is reported as disabled by AmazonSNS.", endpoint.getArn()), e);
			}
		}
		
		String itmsURL = UriBuilders.createITMS(String.format("%s.plist", path));

		LOGGER.debug(itmsURL);

        return URI.create(itmsURL); 
	}
}
