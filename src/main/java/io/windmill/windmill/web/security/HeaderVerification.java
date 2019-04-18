package io.windmill.windmill.web.security;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import io.windmill.windmill.common.Guard;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.web.security.JWT.Header;

public interface HeaderVerification<T extends JWT.Type> extends InvalidClaimGuard, JWTVerification<T> {

	public static <T extends JWT.Type> HeaderVerification<T> make(MacAlgorithm algorithm, Header.Type type) {
		return new HeaderVerification<T>() {

			@Override
			public MacAlgorithm algorithm() {
				return algorithm;
			}

			@Override
			public Header.Type type() {
				return type;
			}
		};
	}

	public static <T extends JWT.Type> JWTVerification<T> make() {
		return new HeaderVerification<T>() {};
	}
			
	public default MacAlgorithm algorithm() {
		return MacAlgorithm.HMAC_SHA256;
	}

	public default Header.Type type() {
		return Header.Type.JWT;
	}

	default JsonObject joseHeader(JWT<T> jws)  {		
		final Decoder decoder = Base64.getUrlDecoder();

		try {
			byte[] decodedHeader = decoder.decode(jws.getHeader().getBytes("UTF-8"));
			return Json.createReader(new StringReader(new String(decodedHeader, Charset.forName("UTF-8")))).readObject();
		} catch (UnsupportedEncodingException | JsonParsingException e) {
			throw new InvalidClaimException("Claim is invalid.", e);
		}
	}
	

	/**
	    3.  Base64url decode the Encoded JOSE Header following the
        	restriction that no line breaks, whitespace, or other additional
        	characters have been used.

   		4. 	Verify that the resulting octet sequence is a UTF-8-encoded
        	representation of a completely valid JSON object conforming to
        	RFC 7159 [RFC7159]; let the JOSE Header be this JSON object.
 
    	5.  Verify that the resulting JOSE Header includes only parameters
        	and values whose syntax and semantics are both understood and
        	supported or that are specified as being ignored when not
        	understood.

	 * <a href="https://tools.ietf.org/html/rfc7519#section-7.2">7.2. Validating a JWT, JSON Web Token (JWT), rfc7519</a>
	 */
	public default void verify(JWT<T> jwt) throws InvalidClaimException {
		try {
			JsonObject header = this.joseHeader(jwt);
			
			String alg = header.getString("alg");
			String typ = header.getString("typ");
		
			MacAlgorithm algorithm = MacAlgorithm.of(alg).orElse(null);
			Header.Type type = Header.Type.of(typ).orElse(null);

			this.guard(this.algorithm().equals(algorithm));
			this.guard(this.type().equals(type));
		} catch (NullPointerException e) {
			throw new InvalidClaimException("Claim is invalid.");
		}
	}
}

interface InvalidClaimGuard extends Guard<InvalidClaimException> {
	
	public default void guard(boolean condition) {
		this.guard(condition, () -> new InvalidClaimException("Claim is invalid."));
	}
}