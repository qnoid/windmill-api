package io.windmill.windmill.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.windmill.windmill.persistence.StatelessEntityManager;
import io.windmill.windmill.persistence.WindmillEntityManager;
import junit.framework.Assert;


public class SubscriptionServiceTest {

	private static EntityManagerFactory emf;
    private static EntityManager em;

    @BeforeClass
    public static void beforeClass() {
		emf = Persistence.createEntityManagerFactory("windmill");
        em = emf.createEntityManager();		
	}
    
	@Before
	public void before() {
		em.getTransaction().begin();
	}
	
	@After
	public void after() {
		em.getTransaction().commit();
	}
	
	@Test
	public void testGivenUpdatedReceiptForExistingSubscriptionAssertExpiryDateUpdate() {
		
		WindmillEntityManager entityManager = new StatelessEntityManager(em);
		SubscriptionService subscriptionService = new SubscriptionService(entityManager, null);
		
		Instant expiresAt = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV").parse("2020-01-31 21:43:24 Etc/GMT"));
		subscriptionService.updateOrCreateSubscription("1000000497931993", "", expiresAt, new Hashtable<>());
		
	}
	
	@Test
	public void testGivenReceiptAssertLatestExpiresTransaction() throws ReceiptVerificationException {
		
		SubscriptionService subscriptionService = new SubscriptionService();
		InputStream is = this.getClass().getResourceAsStream("/receipt.json");
		JsonReader reader = Json.createReader(new InputStreamReader(is));
		JsonObject json = reader.readObject();
		
		JsonObject receipt = json.getJsonObject("receipt");
		JsonArray transactions = receipt.getJsonArray("in_app");

		
		JsonObject transaction = subscriptionService.latest(transactions);
		
		String expiresAt = transaction.getString("expires_date");
		
		Assert.assertEquals(Instant.from(AppStoreService.DATE_FORMATER.parse("2019-01-30 11:17:18 Etc/GMT")), Instant.from(AppStoreService.DATE_FORMATER.parse(expiresAt)));
	}
}
