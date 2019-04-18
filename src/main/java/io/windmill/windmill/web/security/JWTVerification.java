package io.windmill.windmill.web.security;

import java.io.UnsupportedEncodingException;

import io.windmill.windmill.services.exceptions.InvalidSignatureException;

public interface JWTVerification<T extends JWT.Type> {

	void verify(JWT<T> jwt) throws UnsupportedEncodingException, InvalidSignatureException;

}