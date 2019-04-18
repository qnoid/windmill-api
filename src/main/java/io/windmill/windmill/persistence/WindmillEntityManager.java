package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.EntityGraph;
import javax.persistence.NoResultException;

import org.jboss.logging.Logger;

public interface WindmillEntityManager {

    static final Logger LOGGER = Logger.getLogger(WindmillEntityManager.class);

	<T> T find(Class<T> entityClass, Object primaryKey) throws EJBException;
	
	<T> void persist(T obj);

	<T> T merge(T obj) throws EJBException;

	<T> T getSingleResult(String name, QueryConfiguration<T> queryConfiguration) throws NoResultException;

	<T> List<T> getResultList(String name) throws EJBException;

	<T> List<T> getResultList(String name, QueryConfiguration<List<T>> queryConfiguration) throws EJBException;

	<T> EntityGraph<T> getEntityGraph(String graphName) throws EJBException;

    default <T> T findOrProvide(String name, QueryConfiguration<T> queryConfiguration, Provider<T> inCaseOfNoResultException) {
        try {
                return this.getSingleResult(name, queryConfiguration);
        }
        catch (NoResultException e) {
                LOGGER.debug(String.format("NoResultException: %s", e.getMessage()));
            return inCaseOfNoResultException.get();
        }
    }
        
	public static WindmillEntityManager unwrapEJBExceptions(final WindmillEntityManager wem) {
		return new WindmillEntityManager() {
			
			@Override
			public <T> T find(Class<T> entityClass, Object primaryKey) throws EJBException {
				return wem.find(entityClass, primaryKey);
			}
					    
			@Override
			public <T> void persist(T objc) {
				wem.persist(objc);
			}
			
			@Override
			public <T> T merge(T obj) throws EJBException {
				return wem.merge(obj);
			}
			
			@Override
			public <T> T getSingleResult(String name, QueryConfiguration<T> queryConfiguration)
					throws NoResultException {
		    	try {
		        	return wem.getSingleResult(name, queryConfiguration);
		    	} catch (EJBException e) {
		    		Throwable cause = e.getCause();
			
		    		if (cause instanceof NoResultException) {
		    			throw (NoResultException) cause;
		    		} else {
		    			LOGGER.error(e.getMessage(), e);		    			
		    			throw (RuntimeException) e;
		    		}
		    	}
			}
			
			@Override
			public <T> List<T> getResultList(String name) throws EJBException {
				return wem.getResultList(name);
			}

			@Override
			public <T> List<T> getResultList(String name, QueryConfiguration<List<T>> queryConfiguration)
					throws EJBException {
				return wem.getResultList(name, queryConfiguration);
			}

			@Override
			public <T> EntityGraph<T> getEntityGraph(String graphName) throws EJBException {
				return wem.getEntityGraph(graphName);
			}
		};
	}
}