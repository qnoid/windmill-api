package io.windmill.windmill.persistence;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQueries({
    @NamedQuery(name = "subscription.find_by_identifier", query = "SELECT s FROM Subscription s WHERE s.transaction.identifier = :identifier"),
    @NamedQuery(name = "subscription.belongs_to_account_identifier", query = "SELECT s FROM Subscription s WHERE s.account.identifier = :account_identifier AND s.transaction.identifier = :identifier")})
public class Subscription {

	public enum Metadata {		
		WAS_CREATED,
	}
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    @NotNull
    private Instant modifiedAt;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    Account account;

    @OneToOne(mappedBy="subscription")
    @NotNull
    AppStoreTransaction transaction;
    
    /**
     * 
     */
    public Subscription()
    {
    	this.account = new Account();
	    this.createdAt = this.modifiedAt = Instant.now();		
	}
    
    @JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant updatedAt) {
		this.modifiedAt = updatedAt;
	}
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public AppStoreTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(AppStoreTransaction transaction) {
		this.transaction = transaction;
	}	
	
	public String getIdentifier() {
		return transaction.getIdentifier();
	}

	public void setIdentifier(String identifier) {
		transaction.setIdentifier(identifier);
	}

	public Instant getExpiresAt() {
		return transaction.getExpiresAt();
	}

	public boolean hasExpired() {
		return this.transaction.hasExpired();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Subscription))
			return false;
		
		Subscription subscription = (Subscription) that;
		
		return this.id.equals(subscription.id);
	}
}
