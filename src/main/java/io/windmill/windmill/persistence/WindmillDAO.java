// 
// WindmillDAO.java
// windmill
//  
// Created by Markos Charatzas on 21 May 2016.
// Copyright (c) 2016 qnoid.com. All rights reserved.
//
package io.windmill.windmill.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Stateless
public class WindmillDAO {

    @PersistenceContext(name = "windmill-pu")
    private EntityManager em;
    
    public WindmillDAO() {
		super();
	}

	public WindmillDAO(EntityManager em) {
		this.em = em;
	}

    public Windmill findByIdentifier(String identifier) throws NoResultException {
        return (Windmill) em.createNamedQuery("windmill.find_by_identifier")
                .setParameter("identifier", identifier).getSingleResult();
    }
    
    @SuppressWarnings("unchecked")
	public List<Windmill> windmills(String accountIdentifier) {
        return em.createNamedQuery("windmill.with_account_identifier")
                .setParameter("account_identifier", accountIdentifier).getResultList();
    }

	public Windmill findOrCreate(String identifier, Action<Windmill> inCaseOfNoResultException) {

    	try {
    		return this.findByIdentifier(identifier);
    	}
    	catch (NoResultException e) {
    		return inCaseOfNoResultException.create(identifier);
    	}
		
	}
}
