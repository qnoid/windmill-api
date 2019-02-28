package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.jboss.logging.Logger;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Provider;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.sns.Endpoint;

@ApplicationScoped
public class AccountService {

	private static final Logger LOGGER = Logger.getLogger(AccountService.class);

    @Inject
    WindmillEntityManager entityManager;

    @Inject
	NotificationService notificationService;
        
    public AccountService() {
		super();
	}

	public AccountService(WindmillEntityManager entityManager, NotificationService notificationService) {
		this.entityManager = entityManager;
		this.notificationService = notificationService;
	}

	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }
	
	public Account get(UUID account_identifier) throws NoAccountException {
		
		try {
			return this.entityManager.getSingleResult("account.find_by_identifier", identitifier(account_identifier));
		} catch (NoResultException e){
    		throw new NoAccountException("An account for the given identifier doesn't exist. You need an active subscription.", e);
		}
	}    

	public Export updateOrCreate(Account account, String export_identifier, String export_title,
			Double export_version) throws AccountServiceException, NoAccountException {

		try {
			Export export = this.entityManager.findOrProvide("export.find_by_identifier", 
				identitifier(export_identifier), () -> new Export(export_identifier, export_version, export_title));
		
			Condition.guard(export.account == null || export.hasAccount(account), 
					() -> new AccountServiceException("io.windmill.api: error: The bundle identifier is already used by another account."));

			export.setModifiedAt(Instant.now());
			
			account.add(export);
			account.setModifiedAt(Instant.now());		
			this.entityManager.persist(export);
			
			return export;		
		} catch (NoResultException e) {
			throw new NoAccountException("An account for the given identifier doesn't exist. You need an active subscription.");
		}		
	}

	public Device registerDevice(Account account, String token) {
		
		Device device = this.entityManager.findOrProvide("device.find_by_token", query -> query.setParameter("token", token), new Provider<Device>() {
		
			@Override
			public Device get() {
				Device device = new Device(token, account);								
				entityManager.persist(device);				
				return device;
			}
		});

		String endpointArn = notificationService.createPlatformEndpoint(token).getEndpointArn();		
		notificationService.enable(endpointArn);		

		Endpoint endpoint = this.entityManager.findOrProvide("endpoint.find_by_device_token", query -> query.setParameter("device_token", token), new Provider<Endpoint>() {
		
			public Endpoint get() {
				Endpoint endpoint = new Endpoint(endpointArn, device);				
				entityManager.persist(endpoint);				
				return endpoint;
			}			
		});
		
		endpoint.setArn(endpointArn);
		endpoint.setModifiedAt(Instant.now());
				
		LOGGER.debugv("Succesfully registered device with token '%s' for account '%s' with arn '%s'", token, account.getIdentifier(), endpointArn);
	
		return device;
	}
}