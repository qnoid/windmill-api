package io.windmill.windmill.services;

import static org.junit.Assert.assertEquals;

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

    @BeforeClass
    public static void beforeClass() {
		emf = Persistence.createEntityManagerFactory("windmill-pu");
        em = emf.createEntityManager();		
	}
    
	@Before
	public void before() {
		String account_identifier = "14810686-4690-4900-ADA5-8B0B7338AA39";
		em.getTransaction().begin();
		em.persist(new Account(account_identifier));
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
		
		String token = "651743ecad5704a088ff54a0234f37a013bd17b3401d1612cb8ded8af1fa2225";
		Device device = accountService.registerDevice("14810686-4690-4900-ADA5-8B0B7338AA39", token);
		
		assertEquals(token, device.getToken());
	}
}
