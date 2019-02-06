package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.jboss.logging.Logger;

import com.google.common.base.Preconditions;

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
    private WindmillEntityManager entityManager;

    @Inject
	private NotificationService notificationService;
        
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

	private Account get(UUID account_identifier) {
		return this.entityManager.getSingleResult("account.find_by_identifier", identitifier(account_identifier));
	}
    
	public Export updateOrCreate(UUID account_identifier, String export_identifier, String export_title,
			Double export_version) throws IllegalArgumentException {

		Export export = this.entityManager.findOrProvide("export.find_by_identifier", identitifier(export_identifier), () -> new Export(export_identifier, export_version, export_title) );
		
		Preconditions.checkArgument(export.account == null || export.account(account_identifier), "io.windmill.api: error: The bundle identifier is already used by another account.\n");

		export.setModifiedAt(Instant.now());
		
		Account account = this.entityManager.findOrProvide("account.find_by_identifier", identitifier(account_identifier), new Provider<Account>(){

			@Override
			public Account get() {
				Account account = new Account(account_identifier);
				entityManager.persist(account);
				return account;
			}			
		});
		account.setModifiedAt(Instant.now());
		
		LOGGER.debug(String.format("Found: %s", account.getIdentifier()));            
		
		account.add(export);
		
		this.entityManager.persist(account);
		
		return export;
	}
	
	public Device registerDevice(UUID account_identifier, String token) {
		
		Account account;
		
    	try {
    		account = this.get(account_identifier);
    	} catch (NoResultException e){
    		String message = String.format("No account was found for identifier '%s'. This call requires an existing account to register a device for. Hint: POST \"/{account}/export\" creates an account.", account_identifier);    		
			throw new IllegalArgumentException(message);
    	}
    	
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
		
		endpoint.getDevice().setModifiedAt(Instant.now());
		endpoint.setArn(endpointArn);
				
		LOGGER.debugv("Succesfully registered device with token '%s' for account '%s' with arn '%s'", token, account_identifier, endpointArn);
	
		return device;
	}
}