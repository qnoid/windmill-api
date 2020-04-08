//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

package io.windmill.windmill.services;

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
		
		Instant fiveMinutesFromNow = Instant.now().plus(Duration.ofMinutes(5));
		
		Signed<URI> uri = this.authenticationService.manifest(account, export, fiveMinutesFromNow);
		storageService.upload(manifest.getBuffer(), uri);

		return export; 
	}
}
