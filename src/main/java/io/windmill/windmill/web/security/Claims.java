package io.windmill.windmill.web.security;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.resources.InvalidClaimException;
import io.windmill.windmill.web.security.JWT.JWS;

public class Claims<T> {
	
	public static enum Type {
		
		SUBSCRIPTION("sub"),
		ACCESS_TOKEN("at");
		
		public static Optional<Type> of(String value) {
	        for (Type t : Type.values()) {
	            if (t.value.equals(value)) {
	                return Optional.of(t);
	            }
	        }
	
			return Optional.empty();
		}
		
		final String value;

		private Type(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}
	
	public String jti;
	public String sub;
	public Instant exp;
	public Type typ;
	public Long version;

	public static <T extends JWT.Type> Claims<T> create() {
		return new Claims<T>();
	}

	public static Claims<Subscription> subscription(JWT<JWS> jwt) {
		return make(jwt);
	}
	
	public static Claims<SubscriptionAuthorizationToken> accessToken(JWT<JWS> jwt) throws InvalidClaimException {		
		return make(jwt);
	}

	private static <T> Claims<T> make(JWT<JWS> jwt) {
		byte[] decodedPayload = Base64.getUrlDecoder().decode(jwt.payload);		
		JsonObject payload = Json.createReader(new StringReader(new String(decodedPayload, Charset.forName("UTF-8")))).readObject();			
		
		try {			
			String jti = payload.getString("jti");		
			String sub = payload.getString("sub");
			JsonNumber exp = payload.getJsonNumber("exp");			
			Type typ = Claims.Type.of(payload.getString("typ")).get();
			Long version = payload.getJsonNumber("v").longValue();
		
			return new Claims<T>()
					.jti(jti)
					.sub(sub)
					.exp(Instant.ofEpochSecond(exp.longValue()))					
					.typ(typ)
					.version(version);			
		} catch (NullPointerException | IllegalArgumentException e) {
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

	public Claims<T> version(Long version) {
		this.version = version ;
		return this;
	}

	public Claims<T> typ(Claims.Type typ) {
		this.typ = typ;
		return this;
	}

	public boolean hasExpired() {
		if (this.exp == null) {
			return false;
		}
		
		return this.exp.isBefore(Instant.now());
	}

	public boolean isTyp(Type typ) {
		return this.typ.equals(typ);
	}

	public <E> boolean isSub(E sub, Function<String, E> mapper) {
		if (sub == null) {
			return false;
		}
		
		return sub.equals(Optional.ofNullable(this.sub).map(mapper).orElse(null));
	}

	@Override
	public String toString() {
		return String.format("jti:'%s'", this.jti); 
	}
}