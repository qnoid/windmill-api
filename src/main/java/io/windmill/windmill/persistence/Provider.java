package io.windmill.windmill.persistence;

import javax.validation.constraints.NotNull;

@FunctionalInterface
public interface Provider<T> {
	
	public @NotNull T get();
}