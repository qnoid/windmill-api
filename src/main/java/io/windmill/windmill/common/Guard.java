package io.windmill.windmill.common;

import java.util.function.Supplier;

public abstract interface Guard<E extends RuntimeException> {
	
	default public void guard(boolean condition, Supplier<? extends E> orElseThrow) {
		Condition.guard(condition, orElseThrow);
	}
}