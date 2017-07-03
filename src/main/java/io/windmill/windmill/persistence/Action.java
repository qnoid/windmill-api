package io.windmill.windmill.persistence;

public interface Action<T> {
	
	public T create(String identifier);
}