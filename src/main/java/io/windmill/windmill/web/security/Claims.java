package io.windmill.windmill.web.security;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;

import io.windmill.windmill.web.resources.InvalidClaimException;
import io.windmill.windmill.web.security.JWT.JWS;

public class Claims<T extends JWT.Type> {
	
	public String jti;
	public String sub;
	public Instant exp;
	public String typ;

	public static <T extends JWT.Type> Claims<T> create() {
		return new Claims<T>();
	}

	public static Claims<JWS> subscription(JWT<JWS> jwt) {
		
		byte[] decodedPayload = Base64.getUrlDecoder().decode(jwt.payload);		
		JsonObject payload = Json.createReader(new StringReader(new String(decodedPayload, Charset.forName("UTF-8")))).readObject();			
		
		try {
			
			String jti = payload.getString("jti");		
			String sub = payload.getString("sub");
			JsonNumber exp = payload.getJsonNumber("exp");
			String typ = payload.getString("typ");
		
			Claims<JWS> claims = new Claims<JWS>()
					.jti(jti)
					.sub(sub)
					.exp(Instant.ofEpochSecond(exp.longValue()))
					.typ(typ);
			
			return claims;			
		} catch (NullPointerException e) {
			throw new InvalidClaimException("Claim is invalid.");
		}
	}

	public Claims() {
	}

	public Claims<T> jti(String jti) {
		this.jti = jti;
		return this;
	}

	public Claims<T> sub(String sub) {
		this.sub = sub;
		return this;
	}

	public Claims<T> exp(Instant exp) {
		this.exp = exp;
		return this;
	}

	public Claims<T> typ(String typ) {
		this.typ= typ;
		return this;
	}

	public boolean hasExpired() {
		return this.exp.isBefore(Instant.now());
	}

	public boolean hasTyp(String typ) {
		return this.typ.equals(typ);
	}
}