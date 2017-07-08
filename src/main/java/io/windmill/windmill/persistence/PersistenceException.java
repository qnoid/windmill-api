package io.windmill.windmill.persistence;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ws.rs.core.Response.Status;

@ApplicationException
public class PersistenceException extends EJBException {
    private static final long serialVersionUID = 1L;
    public PersistenceException(Status status, String msg) {
        super();
    }
}