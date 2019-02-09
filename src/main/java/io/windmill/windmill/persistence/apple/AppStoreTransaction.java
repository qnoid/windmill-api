package io.windmill.windmill.persistence.apple;

import java.time.Instant;
import java.util.UUID;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@Table(schema="apple",name="transaction")
@NamedQueries({
    @NamedQuery(name = "transaction.find_by_identifier", query = "SELECT t FROM AppStoreTransaction t WHERE t.identifier = :identifier")})
public class AppStoreTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String identifier;

    @NotNull
    private String receipt;

    @Column(name="expires_at")
    @NotNull
    private Instant expiresAt;
    
    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    @NotNull
    private Instant modifiedAt;

    @ManyToOne
    AppStoreTransaction parent;
    
    @OneToOne(cascade = CascadeType.PERSIST)    
    @NotNull
    Subscription subscription;

    /**
     * 
     */
    public AppStoreTransaction()
    {
		this.identifier = UUID.randomUUID().toString();
		this.receipt = "";
		this.expiresAt = Instant.now();
		this.subscription = new Subscription();
		this.subscription.setTransaction(this);
        this.createdAt = this.modifiedAt = Instant.now();		
    }
    
    public AppStoreTransaction(String identifier, String receipt, Instant expiresAt, Subscription subscription) {
		super();
		this.identifier = identifier;
		this.receipt = receipt;
		this.expiresAt = expiresAt;
		this.subscription = subscription;
		this.subscription.setTransaction(this);
        this.createdAt = this.modifiedAt = Instant.now();		
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
       
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}	
	
	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
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

	public AppStoreTransaction getParent() {
		return parent;
	}

	public void setParent(AppStoreTransaction parent) {
		this.parent = parent;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.setTransaction(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof AppStoreTransaction))
			return false;
		
		AppStoreTransaction transaction = (AppStoreTransaction) that;
		
		return this.identifier.equals(transaction.identifier);
	}

	public boolean hasExpired() {
		return this.expiresAt.isBefore(Instant.now());
	}
}
