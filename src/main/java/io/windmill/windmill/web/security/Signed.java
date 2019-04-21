package io.windmill.windmill.web.security;

import java.net.URI;

@FunctionalInterface
public interface Signed<T> {
	
	public URI value();	
}