package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Endpoint;
import io.windmill.windmill.persistence.Provider;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.persistence.WindmillEntityManager;

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

	public <T> T findOrProvide(String name, QueryConfiguration<T> queryConfiguration, Provider<T> inCaseOfNoResultException) {
		try {
			return this.entityManager.getSingleResult(name, queryConfiguration);
		}
		catch (NoResultException e) {
			LOGGER.debug(String.format("NoResultException: %s", e.getMessage()));            			
		    return inCaseOfNoResultException.get();
		}
    }
	
	private Account get(String account_identifier) {
		return this.entityManager.getSingleResult("account.find_by_identifier", identitifier(account_identifier));
	}
    
	public Windmill updateOrCreate(String account_identifier, String windmill_identifier, String windmill_title,
			Double windmill_version) {

		Windmill windmill = this.findOrProvide("windmill.find_by_identifier", identitifier(windmill_identifier), () -> new Windmill(windmill_identifier, windmill_version, windmill_title) );
		windmill.setUpdatedAt(Instant.now());
		
		Account account = this.findOrProvide("account.find_by_identifier", identitifier(account_identifier), new Provider<Account>(){

			@Override
			public Account get() {
				Account account = new Account(account_identifier);
				entityManager.persist(account);
				return account;
			}			
		});
		
		LOGGER.debug(String.format("Found: %s", account.getIdentifier()));            
		
		account.add(windmill);
		
		this.entityManager.persist(account);
		
		return windmill;
	}
	
	public Device registerDevice(String account_identifier, String token) {
		
		Account account;
		
    	try {
    		account = this.get(account_identifier);
    	} catch (NoResultException e){
    		String message = String.format("No account was found for identifier '%s'. This call requires an existing account to register a device for. Hint: POST \"/{account}/windmill\" creates an account.", account_identifier);    		
			throw new IllegalArgumentException(message);
    	}
    	
		Device device = this.findOrProvide("device.find_by_token", query -> query.setParameter("token", token), new Provider<Device>() {
		
			@Override
			public Device get() {
				Device device = new Device(token, account);				
				
				entityManager.persist(device);
				
				return device;
			}
		});

		String endpointArn = notificationService.createPlatformEndpoint(token).getEndpointArn();		
		notificationService.enable(endpointArn);		

		Endpoint endpoint = this.findOrProvide("endpoint.find_by_device_token", query -> query.setParameter("device_token", token), () -> new Endpoint(endpointArn, device));
		endpoint.getDevice().setUpdatedAt(Instant.now());
		endpoint.setArn(endpointArn);
		
		this.entityManager.merge(endpoint);
		
		LOGGER.debugv("Succesfully registered device with token '%s' for account '%s' with arn '%s'", token, account_identifier, endpointArn);
	
		return device;
	}
}