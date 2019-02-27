package io.windmill.windmill.common;

import java.util.function.Supplier;

public class Condition {

	public static boolean doesnot(boolean condition) {
		return !condition;
	}
	
    public static <X extends Throwable> void doesnot(boolean condition, Supplier<? extends X> willThrow) throws X {
        if (!condition)
            throw willThrow.get();
    }
}
