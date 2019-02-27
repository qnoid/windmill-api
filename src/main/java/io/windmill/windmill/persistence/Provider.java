package io.windmill.windmill.persistence;

import javax.validation.constraints.NotNull;

public interface Provider<T> {

	public @NotNull T get();	
}
