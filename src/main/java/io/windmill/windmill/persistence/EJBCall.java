package io.windmill.windmill.persistence;

import javax.ejb.EJBException;

@FunctionalInterface
public interface EJBCall<T> {

	public T make() throws EJBException;
}
