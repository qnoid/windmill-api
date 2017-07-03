package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * @author javapapo on 19/05/2016.
 */
@Stateless
public class AccountDAO {

	@PersistenceContext(name = "windmill-pu")
    private EntityManager em;

    
    public AccountDAO(EntityManager em) {
		this.em = em;
	}
    
	public AccountDAO() {		
	}

	void persist(Account account) {
		this.em.persist(account);
	}

    /**
     * TODO
     * @return
     */
    Account findByIdentifier(String identifier) throws NoResultException {
        return (Account) em.createNamedQuery("account.find_by_identifier")
                .setParameter("identifier", identifier).getSingleResult();
    }


	/**
     * TODO
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<Account> accounts() {
        return em.createNamedQuery("account.list").getResultList();
    }
    
    public Account findOrCreate(String identifier) {
    	
    	try {
    		return this.findByIdentifier(identifier);
    	}
    	catch (NoResultException e) {
    		Account account = new Account(identifier);    		
    		this.persist(account);
    		
    		return account;
    	}
    }

	public boolean saveOrUpdate(Account user) {
		try {
			this.em.persist(user);
			return true;
			
		}catch (EntityExistsException e) {
			this.em.merge(user);
			
			return false;
		}
	}    
}
