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
import javax.ejb.Stateless;
import javax.enterprise.inject.Default;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

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
	public <T> T getSingleResult(CriteriaQuery<T> query, QueryConfiguration<T> queryConfiguration) throws EJBException {
		return (T) queryConfiguration.apply(em.createQuery(query)).getSingleResult();
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
	
	@Override
	public CriteriaBuilder getCriteriaBuilder() throws EJBException {
		return this.em.getCriteriaBuilder();
	}

}
