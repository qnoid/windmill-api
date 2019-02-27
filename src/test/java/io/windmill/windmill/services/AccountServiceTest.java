package io.windmill.windmill.services;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.StatelessEntityManager;
import io.windmill.windmill.persistence.WindmillEntityManager;


public class AccountServiceTest {

	private static EntityManagerFactory emf;
    private static EntityManager em;

    String account_identifier = "14810686-4690-4900-ada5-8b0b7338aa39";
    
    @BeforeClass
    public static void beforeClass() {
		emf = Persistence.createEntityManagerFactory("windmill");
        em = emf.createEntityManager();		
	}
    
	@Before
	public void before() {
		em.getTransaction().begin();
		em.persist(new Account(UUID.fromString(account_identifier)));
		em.flush();
	}
	
	@After
	public void after() {
		em.getTransaction().commit();
	}
	
	@Test
	public void testGivenAccountAssertRegisterDeviceSuccess() {

		WindmillEntityManager entityManager = new StatelessEntityManager(em);
		NotificationService notificationService = new NotificationService();
		AccountService accountService = new AccountService(entityManager, notificationService);
		
		Account account = accountService.get(UUID.fromString(account_identifier));
		String token = "651743ecad5704a088ff54a0234f37a013bd17b3401d1612cb8ded8af1fa2225";
		Device device = accountService.registerDevice(account, token);
		
		assertEquals(token, device.getToken());
	}
}
