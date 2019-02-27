package io.windmill.windmill.persistence.web;

import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Optional;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.common.Secret;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;


@Entity
@Table(schema="secret", name="authorization_token")
public class AuthorizationToken {

	static public AuthorizationToken create() {		
		return new AuthorizationToken(Secret.create(16));		
	}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
	private String value;

    @Column(name="created_at")
    private Instant createdAt;

    transient private byte[] bytes;
    
	AuthorizationToken() {
		Secret<AuthorizationToken> secret = Secret.create(16);		
		this.bytes = secret.getBytes();
		this.value = secret.encoded().get();
		this.createdAt = Instant.now();
	}
	
	AuthorizationToken(Secret<AuthorizationToken> secret) {
		this.bytes = secret.getBytes();
		this.value = secret.encoded().get();
		this.createdAt = Instant.now();
	}
	
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getValue() {		
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	
	@Override
	public String toString() {
		Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		
		return Optional.ofNullable(this.bytes).map(bytes -> encoder.encodeToString(bytes)).orElse(this.value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof AuthorizationToken))
			return false;
		
		AuthorizationToken authorizationToken = (AuthorizationToken) that;
		
		return this.value.equals(authorizationToken.value);		
	}	
}
