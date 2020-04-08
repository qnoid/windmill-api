
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.web.CustomJsonUUIDSerializer;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQuery(name = "account.list", query = "SELECT a FROM Account a")
@NamedQuery(name = "account.find_by_identifier", query = "SELECT a FROM Account a WHERE a.identifier = :identifier")
public class Account {

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
    
    @OneToMany(mappedBy="account", fetch=FetchType.LAZY)
    private Set<Export> exports;

    @OneToMany(mappedBy="account", fetch=FetchType.LAZY)
    private Set<Device> devices;
    
    /**
     * 
     */
    public Account()
    {
    	this.identifier = UUID.randomUUID();
	    this.createdAt = Instant.now();		
		this.exports = new HashSet<Export>();
		this.devices = new HashSet<Device>();
    }
        
	public Account(UUID identifier) {
		this.identifier = identifier;
	    this.createdAt = Instant.now();		
		this.exports = new HashSet<Export>();
		this.devices = new HashSet<Device>();
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
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
		
	public Set<Export> getExports() {
		return exports;
	}

	public void setExports(Set<Export> exports) {
		this.exports = exports;
	}

	public Set<Device> getDevices() {
		return devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}

	public void add(Export export) {
		export.account = this;		
		this.exports.add(export);
		this.setModifiedAt(Instant.now());		
	}
	
	public void add(Device device) {
		device.account = this;		
		this.devices.add(device);
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
		
		if (!(that instanceof Account))
			return false;
		
		Account account = (Account) that;
		
		return this.identifier.equals(account.identifier);
	}

	@Override
	public String toString() {
		return String.format("{identifier:%s}", this.identifier);
	}
}
