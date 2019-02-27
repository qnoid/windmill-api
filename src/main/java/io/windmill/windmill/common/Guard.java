package io.windmill.windmill.common;

import static io.windmill.windmill.common.Condition.doesnot;

import java.util.function.Supplier;

public abstract interface Guard<E extends RuntimeException> {
	
	default public void guard(boolean condition, Supplier<? extends E> orElseThrow) {
		doesnot(condition, orElseThrow);
	}
}