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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Metadata;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.StatelessEntityManager;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.web.common.Configuration;

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
		Account account = new Account(UUID.fromString(account_identifier));
		Export export = new Export("io.windmill.windmill", "1.0", "windmill");
		account.add(export);
		Metadata metadata = new Metadata();
		metadata.setTarget("12.2");
		metadata.setBranch("master");
		metadata.setShortSha("aa37136");
		metadata.setConfiguration(Configuration.RELEASE);
		metadata.setCertificateExpiryDate(Instant.now().plus(1, ChronoUnit.DAYS));
		export.setMetadata(metadata);
		
		em.getTransaction().begin();		
		em.persist(account);
		em.persist(export);
		em.persist(metadata);
		em.flush();
	}
	
	@After
	public void after() {
		em.getTransaction().commit();
	}
	
	@Test
	public void testGivenAccountAssertExportsSuccess() {

		WindmillEntityManager entityManager = new StatelessEntityManager(em);
		NotificationService notificationService = new NotificationService();
		AccountService accountService = new AccountService(entityManager, notificationService);
		
		Account account = accountService.get(UUID.fromString(account_identifier));		
		Set<Export> exports = account.getExports();
		
		assertEquals(1, exports.size());
		Export export = exports.iterator().next();
		Metadata build = export.getMetadata();
		
		assertNotNull(build);
		assertEquals("12.2", build.getTarget());		
		assertEquals("aa37136", build.getShortSha());
	}
}
