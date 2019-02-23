package io.windmill.windmill.common;

import static io.windmill.windmill.common.Condition.doesnot;

import io.windmill.windmill.persistence.Provider;

public abstract interface Guard<E extends RuntimeException> {
	
	default public void apply(boolean condition, Provider<E> provider) {
		if (doesnot(condition)){
			throw provider.get();
		}
	}
}