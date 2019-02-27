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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@Table(name="subscription_authorization_token")
@NamedQueries(
    @NamedQuery(name = "subscription_authorization_token.belongs_to_subscription_identifier", query = "SELECT sat FROM SubscriptionAuthorizationToken sat WHERE sat.subscription.transaction.identifier = :subscription_identifier AND sat.authorizationToken.value = :authorization_token")
    )
public class SubscriptionAuthorizationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="accessed_at")
    private Instant accessedAt;

    @OneToOne(cascade = CascadeType.PERSIST)    
    @NotNull
    @JoinColumn(name="authorization_token_id")
    AuthorizationToken authorizationToken;

    @OneToOne
    @JoinColumn(name="subscription_id")
    @NotNull
    Subscription subscription;

	public SubscriptionAuthorizationToken() {
		this.authorizationToken = AuthorizationToken.create();
        this.createdAt = Instant.now();
	}
	
	public SubscriptionAuthorizationToken(Subscription subscription) {
		super();
		this.authorizationToken = AuthorizationToken.create();
		this.subscription = subscription;
        this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizationToken == null) ? 0 : authorizationToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof SubscriptionAuthorizationToken))
			return false;
		
		SubscriptionAuthorizationToken subscriptionAuthorizationToken = (SubscriptionAuthorizationToken) that;
		
		return this.authorizationToken.equals(subscriptionAuthorizationToken.authorizationToken); 
	}	
}
