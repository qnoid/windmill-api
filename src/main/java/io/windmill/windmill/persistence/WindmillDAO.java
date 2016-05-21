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
import javax.persistence.PersistenceContext;

@Stateless
public class WindmillDAO {

    @PersistenceContext(name = "windmill-pu")
    private EntityManager em;

    /**
     * TODO
     * @return
     */
    public List<Windmill> windmills(String userIdentifier) {
        return em.createNamedQuery("windmill.with_user_identifier")
                .setParameter("user_identifier", userIdentifier).getResultList();
    }
}
