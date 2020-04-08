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

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.apache.commons.configuration2.ex.ConfigurationException;

import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Metadata;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.sns.Endpoint;
import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.common.ContentType;
import io.windmill.windmill.services.exceptions.ExportGoneException;
import io.windmill.windmill.services.exceptions.ExportServiceException;
import io.windmill.windmill.services.exceptions.StorageServiceException;
import io.windmill.windmill.web.common.ApplicationProperties;
import io.windmill.windmill.web.common.Build;
import io.windmill.windmill.web.common.Commit;
import io.windmill.windmill.web.common.Deployment;
import io.windmill.windmill.web.common.DistributionSummary;
import io.windmill.windmill.web.security.Signed;

@ApplicationScoped
public class ExportService {

    @Inject
    private AuthenticationService authenticationService;    

    @Inject 
    private NotificationService notificationService; 

	@Inject
    private WindmillEntityManager entityManager;

    @Inject 
    private StorageService storageService;

	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public Export get(UUID export_identifier) throws ExportGoneException {		
				
		try {
			return this.entityManager.getSingleResult("export.find_by_identifier", identitifier(export_identifier));	
		} catch (NoResultException e) {
			throw new ExportGoneException(ExportGoneException.EXPORT_NOT_FOUND, e);
		}		
	}

	public Export belongs(UUID account_identifier, UUID export_identifier) throws ExportGoneException {
		try {
			
			Export export = this.entityManager.getSingleResult("export.belongs_to_account_identifier", new QueryConfiguration<Export>() {
		
				@Override
				public @NotNull Query apply(Query query) {
					query.setParameter("identifier", export_identifier);
					query.setParameter("account_identifier", account_identifier);
					
					return query;
				}
			});
			
			return export;
		} catch (NoResultException e) {
			throw new ExportGoneException(ExportGoneException.EXPORT_NOT_FOUND, e);
		}
	}    
	
	public void notify(Export export) 
	{
		String notification = Messages.of("New build", 
				String.format("%s %s (%s) is now available to install.", 
						export.getTitle(), 
						export.getVersion(), 
						export.getMetadata().getShortSha()));

		String contentAvailable = Messages.contentAvailable(ContentType.EXPORT);

		List<Endpoint> endpoints = 
				this.entityManager.getResultList("endpoint.find_by_account_identifier", 
						query -> query.setParameter("account_identifier", export.getAccount().getIdentifier())); 
		
		this.notificationService.notify(contentAvailable, endpoints);
		this.notificationService.notify(notification, endpoints);
	}

	public void delete(Export export) throws StorageServiceException {

		this.entityManager.delete(export);
		
		Instant fiveMinutesFromNow = Instant.now().plus(Duration.ofMinutes(5));

		this.storageService.delete(this.authenticationService.export(export, fiveMinutesFromNow));
		this.storageService.delete(this.authenticationService.manifest(export, fiveMinutesFromNow));
		
	}

	public Export update(UUID export_identifier, Build build) {
		
		Export export = this.get(export_identifier);
		Metadata metadata = Optional.ofNullable(export.getMetadata()).orElse(new Metadata());
		metadata.setConfiguration(build.getConfiguration());
		Commit commit = build.getCommit();
		metadata.setShortSha(commit.getShortSha());
		metadata.setBranch(commit.getBranch());
		metadata.setDate(commit.getDate());
		ApplicationProperties applicationProperties = build.getApplicationProperties();
		metadata.setBundleDisplayName(applicationProperties.getBundleDisplayName());
		metadata.setBundleVersion(applicationProperties.getBundleVersion());
		Deployment deployment = build.getDeployment();
		metadata.setTarget(deployment.getTarget());
		DistributionSummary distributionSummary = build.getDistributionSummary();
		metadata.setCertificateExpiryDate(distributionSummary.getCertificateExpiryDate());
		export.setMetadata(metadata);
		
		this.entityManager.persist(export);
		
		return export;
	}

	public Manifest manifest(Export export, Signed<URI> uri) {

		try {
			Manifest manifest = this.storageService.get(uri);
		
			Instant fiveMinutesFromNow = Instant.now().plus(Duration.ofMinutes(5));		
			Signed<URI> url = this.authenticationService.export(export, fiveMinutesFromNow);

			HashMap<String, Object> substitutions = new HashMap<>(); 
			substitutions.put("URL", url.value().toString());
			
			ByteArrayOutputStream byteArrayOutputStream = 
					new MustacheWriter().substitute(manifest.getBuffer(), substitutions);

			return Manifest.manifest(byteArrayOutputStream);
		} catch (ConfigurationException | IOException e) {
			throw new ExportServiceException(e.getMessage(), e);
		}
	}
}
