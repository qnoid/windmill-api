package io.windmill.windmill.common;

import java.util.function.Supplier;

public class Condition {

	public static boolean doesnot(boolean condition) {
		return !condition;
	}
	
    public static <X extends Throwable> void guard(boolean condition, Supplier<? extends X> elseThrow) throws X {
        if (!condition)
            throw elseThrow.get();
    }
}
