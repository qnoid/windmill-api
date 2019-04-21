package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.inject.Default;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Default
public class StatelessEntityManager implements WindmillEntityManager {

	@PersistenceContext(name = "windmill")
    private EntityManager em;

    public StatelessEntityManager() {
		super();
	}

	public StatelessEntityManager(EntityManager em) {
		this.em = em;
	}

	@Override
	public <T> void persist(T obj) {
		this.em.persist(obj);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) throws EJBException {
		return this.em.find(entityClass, primaryKey);
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
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> EntityGraph<T> getEntityGraph(String graphName) throws EJBException {
		return (EntityGraph<T>) this.em.getEntityGraph(graphName);
	}
	
	@Override
	public <T> void delete(T entity) {
		this.em.remove(entity);
	}
}
