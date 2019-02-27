package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

/**
 * A {@link Stateful} bean to use when a {@link PersistenceContextType#EXTENDED} context is required.
 * 
 * This is useful when you want to return an {@link Entity} from a JAX-RS Resource that holds {@link FetchType#LAZY} references 
 * that need to be serialised outside the {@link Transactional} scope of the resource.
 * 
 *  e.g. by JSON-B
 * 
 * The `ExtendedEntityManager` should only be used within a {@link RequestScoped} or a {@link SessionScoped} bean.
 * Associating it with a {@link ApplicationScoped} will lead to locking and potentially a {@link ConcurrentAccessTimeoutException} (default 5000 MILLISECONDS)
 * 
 * @author qnoid
 */
@Stateful
@Extended
public class ExtendedEntityManager implements WindmillEntityManager {

	@PersistenceContext(name = "windmill-extended", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public ExtendedEntityManager() {
		super();
	}

	public ExtendedEntityManager(EntityManager em) {
		this.em = em;
	}
	
	@Override
	public <T> void persist(T obj) {
		this.em.persist(obj);
	}

	@Override
    @SuppressWarnings("unchecked")
	public <T> T getSingleResult(String name, QueryConfiguration<T> queryConfiguration) throws EJBException {
		return (T) queryConfiguration.apply(em.createNamedQuery(name)).getSingleResult();
    }

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList(String name) throws EJBException {
        return (List<T>) em.createNamedQuery(name).getResultList();
    }
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList(String name, QueryConfiguration<List<T>> queryConfiguration) throws EJBException {
        return (List<T>) queryConfiguration.apply(em.createNamedQuery(name)).getResultList();
    }
    
	@Override
	public <T> T merge(T obj) throws EJBException {
		return this.em.merge(obj);
	}
}
