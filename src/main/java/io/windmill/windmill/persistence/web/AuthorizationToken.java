package io.windmill.windmill.persistence.web;

import java.time.Instant;
import java.util.Base64;
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
    @NotNull
    private Instant createdAt;

    transient private byte[] bytes;
    
	AuthorizationToken() {
		Secret secret = Secret.create();
		this.bytes = secret.getBytes();
		this.value = secret.encoded().get();		
		this.createdAt = Instant.now();
	}
	
	AuthorizationToken(Secret secret) {
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
		return Optional.ofNullable(this.bytes).map(bytes -> Base64.getEncoder().encodeToString(bytes)).orElse(this.value);
	}
}
