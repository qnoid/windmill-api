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

package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.EntityGraph;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.jboss.logging.Logger;

public interface WindmillEntityManager {

    static final Logger LOGGER = Logger.getLogger(WindmillEntityManager.class);

	<T> T find(Class<T> entityClass, Object primaryKey) throws EJBException;
	
	<T> void persist(T obj);

	<T> T merge(T obj) throws EJBException;

	<T> T getSingleResult(CriteriaQuery<T> query, QueryConfiguration<T> queryConfiguration) throws NoResultException;
	
	<T> T getSingleResult(String name, QueryConfiguration<T> queryConfiguration) throws NoResultException;

	<T> List<T> getResultList(String name) throws EJBException;

	<T> List<T> getResultList(String name, QueryConfiguration<List<T>> queryConfiguration) throws EJBException;

	<T> EntityGraph<T> getEntityGraph(String graphName) throws EJBException;

	<T> void delete(T entity);
	
	CriteriaBuilder getCriteriaBuilder() throws EJBException;
	
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
			public <T> T getSingleResult(CriteriaQuery<T> query, QueryConfiguration<T> queryConfiguration) throws EJBException {
		    	try {
		        	return wem.getSingleResult(query, queryConfiguration);
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

			@Override
			public <T> void delete(T entity) {
				wem.delete(entity);
			}

			@Override
			public CriteriaBuilder getCriteriaBuilder() throws EJBException {
				return wem.getCriteriaBuilder();
			}
		};
	}
}