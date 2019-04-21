package io.windmill.windmill.services;

import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Provider;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.sns.Endpoint;

@ApplicationScoped
public class DeviceService {
	
	private static final Logger LOGGER = Logger.getLogger(DeviceService.class);

    @Inject
    WindmillEntityManager entityManager;

    @Inject
	NotificationService notificationService;
        
    public DeviceService() {
		super();
	}

	public DeviceService(WindmillEntityManager entityManager, NotificationService notificationService) {
		this.entityManager = entityManager;
		this.notificationService = notificationService;
	}

	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }
	
	public Device updateOrCreate(Account account, String token) {
		
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
