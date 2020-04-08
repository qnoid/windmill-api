//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

package io.windmill.windmill.persistence;

import java.time.Instant;
import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.web.CustomJsonUUIDSerializer;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQuery(name = "subscription.find_by_identifier", query = "SELECT s FROM Subscription s WHERE s.identifier = :identifier")
@NamedQuery(name = "subscription.belongs_to_account_identifier", query = "SELECT s FROM Subscription s WHERE s.account.identifier = :account_identifier AND s.identifier = :identifier")
@NamedEntityGraph(name = "subscription.exports", 
	attributeNodes = 
		@NamedAttributeNode(value = "account", subgraph = "account"), 
	subgraphs = {
		@NamedSubgraph(name = "account", attributeNodes = 
			@NamedAttributeNode(value = "exports")),
		})
@NamedEntityGraph(name = "subscription.transaction", attributeNodes = @NamedAttributeNode(value = "transaction"))
public class Subscription {

	public enum Metadata {		
		WAS_CREATED
	}
	
	public enum Fetch {
		EXPORTS("subscription.exports"),
		TRANSACTION("subscription.transaction");
		
		private String entityGraph;
	
		private Fetch(String entityGraph) {
			this.entityGraph = entityGraph;
		}
	
		public String getEntityGraph() {
			return entityGraph;
		}
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private UUID identifier;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="modified_at")
    private Instant modifiedAt;

    @Column(name="accessed_at")
    private Instant accessedAt;

    @ManyToOne(cascade = CascadeType.PERSIST)
    Account account;

    @OneToOne(mappedBy="subscription", fetch=FetchType.LAZY)
    @NotNull
    AppStoreTransaction transaction;
    
    /**
     * 
     */
    public Subscription()
    {
    	this.identifier = UUID.randomUUID();    	
	    this.createdAt = Instant.now();	
	}

	@JsonbTypeSerializer(CustomJsonUUIDSerializer.class)
	public UUID getIdentifier() {
		return identifier;
	}

	public void setIdentifier(UUID identifier) {
		this.identifier = identifier;
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

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant modifiedAt) {
		this.modifiedAt = modifiedAt;
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
	
	public Instant getExpiresAt() {
		return transaction.getExpiresAt();
	}

	public boolean isActive() {
		return !this.transaction.hasExpired();
	}

	public boolean hasExpired() {
		return this.transaction.hasExpired();
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
		
		if (!(that instanceof Subscription))
			return false;
		
		Subscription subscription = (Subscription) that;
		
		return this.identifier.equals(subscription.identifier);
	}
}
