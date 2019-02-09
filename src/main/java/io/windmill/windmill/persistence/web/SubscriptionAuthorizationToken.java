package io.windmill.windmill.persistence.web;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@Table(name="subscription_authorization_token")
public class SubscriptionAuthorizationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="accessed_at")
    private Instant accessedAt;

    @OneToOne(cascade = CascadeType.PERSIST)    
    @NotNull
    @JoinColumn(name="authorization_token_id")
    AuthorizationToken authorizationToken;

    @OneToOne(cascade = CascadeType.MERGE)    
    @NotNull
    Subscription subscription;

	public SubscriptionAuthorizationToken() {
		// TODO Auto-generated constructor stub
	}
	
	public SubscriptionAuthorizationToken(Subscription subscription) {
		super();
		this.authorizationToken = AuthorizationToken.create();
		this.subscription = subscription;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getAccessedAt() {
		return accessedAt;
	}

	public void setAccessedAt(Instant accessedAt) {
		this.accessedAt = accessedAt;
	}

	public AuthorizationToken getAuthorizationToken() {
		return authorizationToken;
	}

	public void setAuthorizationToken(AuthorizationToken authorizationToken) {
		this.authorizationToken = authorizationToken;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	@Override
	public String toString() {
		return this.authorizationToken.toString();
	}
}
