package io.windmill.windmill.persistence;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class AcccountDAOTest {

	private EntityManagerFactory emf;	
	private EntityManager em;

	@Before
	public void before() {
		emf = Persistence.createEntityManagerFactory("windmill-pu");
		em = emf.createEntityManager();
	}
	
	@After
	public void after() {
		em.close();
		emf.close();		
	}
	
	@Test(expected = NoResultException.class)
	public void testGivenUnknownIdentifierAssertNoResultException() {	
		AccountDAO accountDAO = new AccountDAO(em);
        accountDAO.findByIdentifier("unknown");                
	}
	
	@Test
	public void testGivenUnknownIdentifierAssertAccount() {
		AccountDAO accountDAO = new AccountDAO(em);
        String identifier = UUID.randomUUID().toString();
		Account account = accountDAO.findOrCreate(identifier);
		
		assertTrue(identifier == account.getIdentifier());
	}

	@Test
	public void testGivenExistingnIdentifierAssertAccount() {
		
		//given
		AccountDAO accountDAO = new AccountDAO(em);
        String identifier = UUID.randomUUID().toString();
        
        Runnable persistAccount = () -> accountDAO.persist(new Account(identifier));
        
        this.transactional(persistAccount);
        
        //when
		Account account = accountDAO.findOrCreate(identifier);
		
		assertTrue(identifier == account.getIdentifier());
	}

	@Test
	public void testGivenAccountAssertSave() {
		
		AccountDAO acccountDAO = new AccountDAO(em);
		
		Account acccount = new Account(UUID.randomUUID().toString());
		boolean saved = acccountDAO.saveOrUpdate(acccount);
		
		assertTrue(saved);
	}

	private void transactional(Runnable runnable) {
		EntityTransaction trx = em.getTransaction();
		trx.begin();		
		runnable.run();
		trx.commit();
	}
	
	@Test
	public void testGivenAccountAssertUpdate() {
		
		//given
		String account_identifier = UUID.randomUUID().toString();
		AccountDAO accountDAO = new AccountDAO(em);		
		
		//when
		Windmill windmill = new Windmill(UUID.randomUUID().toString(), 1.0, "title");
		
		Runnable saveOrUpdateAccount = () -> {
			
			Account account = accountDAO.findOrCreate(account_identifier);
			account.add(windmill);
			accountDAO.saveOrUpdate(account);	
		};

		this.transactional(saveOrUpdateAccount);
		
		Assert.assertNotNull(windmill.getId());
		Assert.assertTrue(windmill.account(account_identifier));		
	}
	
	@Test
	public void testGivenUpdateOnAccountWithExistingWindmillAssertNoDuplicate() {
		
		WindmillDAO windmillDAO = new WindmillDAO(em);		

		//given
		String windmill_identifier = UUID.randomUUID().toString();
		String account_identifier = UUID.randomUUID().toString();
		AccountDAO accountDAO = new AccountDAO(em);		

		Runnable given = () -> {
			
			Account account = accountDAO.findOrCreate(account_identifier);
			account.add(new Windmill(windmill_identifier, 1.0, "title"));
			accountDAO.saveOrUpdate(account);	
		};

		this.transactional(given);
		
		int actual = windmillDAO.windmills(account_identifier).size();

		Assert.assertTrue(Integer.toString(actual), 1 == actual);

		//when		
		
		Windmill windmill = new Windmill(windmill_identifier, 1.0, "title");
		
		Runnable runnable = () -> {
			
			Account account = accountDAO.findOrCreate(account_identifier);
			account.add(windmill);
			accountDAO.saveOrUpdate(account);	
		};
		
		this.transactional(runnable);
				
		int size = windmillDAO.windmills(account_identifier).size();
		
		Assert.assertTrue(Integer.toString(size), 1 == size);
	}
}
