package io.windmill.windmill.persistence;

@FunctionalInterface
public interface Action<T> {
	
	public T create(String identifier);
}