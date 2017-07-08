package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;

public interface WindmillEntityManager {

	<T> void persist(T obj);

	<T> T merge(T obj) throws EJBException;

	<T> T getSingleResult(String name, QueryConfiguration<T> queryConfiguration) throws NoResultException;

	<T> List<T> getResultList(String name) throws EJBException;

	<T> List<T> getResultList(String name, QueryConfiguration<List<T>> queryConfiguration) throws EJBException;


	public static WindmillEntityManager unwrapEJBExceptions(final WindmillEntityManager wem) {
		return new WindmillEntityManager() {
			
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
		};
	}
}