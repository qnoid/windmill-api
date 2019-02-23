package io.windmill.windmill.web.security;

import java.io.UnsupportedEncodingException;

public interface JWTVerification<T extends JWT.Type> {

	void verify(JWT<T> jwt) throws UnsupportedEncodingException;

}