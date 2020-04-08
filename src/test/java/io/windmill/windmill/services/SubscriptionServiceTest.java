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
import io.windmill.windmill.services.apple.AppStoreService;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;
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
