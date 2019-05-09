package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Metadata;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.sns.Endpoint;
import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.common.ContentType;
import io.windmill.windmill.services.exceptions.ExportGoneException;
import io.windmill.windmill.services.exceptions.StorageServiceException;
import io.windmill.windmill.web.common.ApplicationProperties;
import io.windmill.windmill.web.common.Build;
import io.windmill.windmill.web.common.Commit;
import io.windmill.windmill.web.common.Deployment;
import io.windmill.windmill.web.common.DistributionSummary;

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
}
