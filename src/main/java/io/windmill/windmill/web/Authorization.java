package io.windmill.windmill.web;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

public class Authorization {

	public static JWT<JWS> get(String header) throws NoSuchElementException {
		
		final String Bearer = "Bearer";
		
		return Optional.ofNullable(header)
				.filter( authorization -> authorization.toLowerCase().startsWith((Bearer.toLowerCase()) + " ") )
				.map( authorization -> JWT.jws(authorization.substring(Bearer.length()).trim()) )
				.get();
	}
}
