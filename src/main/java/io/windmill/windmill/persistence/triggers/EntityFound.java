package io.windmill.windmill.persistence.triggers;

@FunctionalInterface
public interface EntityFound<T> {
	public void trigger(T entity);
}