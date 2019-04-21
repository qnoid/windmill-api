package io.windmill.windmill.services;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilderException;

import org.jboss.logging.Logger;

import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.services.exceptions.AccountServiceException;
import io.windmill.windmill.services.exceptions.NoAccountException;
import io.windmill.windmill.services.exceptions.StorageServiceException;
import io.windmill.windmill.web.Host;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;
import io.windmill.windmill.web.security.Signed;

@ApplicationScoped
public class WindmillService {

    static final Logger LOGGER = Logger.getLogger(WindmillService.class);

    @Inject
    private AuthenticationService authenticationService;    

    @Inject
    private AccountService accountService; 

    @Inject 
    private StorageService storageService;
    
    @Inject 
    private WindmillEntityManager entityManager;

    @PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	/**
	 * 
     * @precondition the account <b>must</b> exist  
	 * @param account
	 * @param manifest
	 * @return a URI in the itms format, e.g. "itms-services://?action=download-manifest&url=https://ota.windmill.io/14810686-4690-4900-ADA5-8B0B7338AA39/io.windmill.windmill/1.0/windmill.plist"
	 * @throws FileNotFoundException if the given ipa is not found
	 * @throws URISyntaxException 
	 * @throws UriBuilderException 
	 * @throws AccountServiceException 
	 * @throws StorageServiceException 
	 */
	public Export updateOrCreate(Account account, Manifest manifest) throws AccountServiceException, NoAccountException, StorageServiceException 
	{
		Export export = this.accountService.updateOrCreate(account, manifest.getBundle(), manifest.getTitle(), manifest.getVersion());
	
	    LOGGER.debug(String.format("Created or updated export for account '%s' with metadata.bundle-identifier '%s', metadata.bundle-version '%s', metadata.title '%s'", account, export.getIdentifier(), export.getVersion(), export.getTitle()));

		JWT<JWS> jwt = this.authenticationService.jwt(export);		    
		URI url = Host.API_DOMAIN.account(account).path("export").path(jwt.toString()).build();
		
		ByteArrayOutputStream byteArrayOutputStream = manifest.plistWithURLString(url.toString());
		
		Instant fifteenMinutesFromNow = Instant.now().plus(Duration.ofMinutes(15));
		
		Signed<URI> uri = this.authenticationService.manifest(account, export, fifteenMinutesFromNow);
		System.out.println(storageService.upload(byteArrayOutputStream, uri));

		return export; 
	}
}
