package io.windmill.windmill.persistence;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author javapapo on 19/05/2016.
 */
@Stateless
public class UserDAO {

    @PersistenceContext(name = "windmill-pu")
    private EntityManager em;

    /**
     * TODO
     * @return
     */
    public List<User> users() {
        return em.createNamedQuery("user.list").getResultList();
    }

    /**
     * TODO
     * @return
     */
    public List<User> windmills(String userIdentifier) {
        return em.createNamedQuery("windmill.with_user_identifier")
                .setParameter("user_identifier", userIdentifier).getResultList();
    }
}
