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


public class DeviceServiceTest {

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
		DeviceService deviceService = new DeviceService(entityManager, notificationService);
		
		Account account = accountService.get(UUID.fromString(account_identifier));
		String token = "651743ecad5704a088ff54a0234f37a013bd17b3401d1612cb8ded8af1fa2225";
		Device device = deviceService.updateOrCreate(account, token);
		
		assertEquals(token, device.getToken());
	}
}
