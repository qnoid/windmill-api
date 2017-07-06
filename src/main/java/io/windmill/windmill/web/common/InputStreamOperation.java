package io.windmill.windmill.web.common;

import java.io.InputStream;

@FunctionalInterface
public interface InputStreamOperation<T> {
	
	public T apply(InputStream stream);
}