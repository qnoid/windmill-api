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

package io.windmill.windmill.persistence.sns;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@Table(schema="sns")
@NamedQuery(name = "endpoint.find_by_device_token", query = "SELECT e FROM Endpoint e WHERE e.device.token = :device_token")
@NamedQuery(name = "endpoint.find_by_account_identifier", query = "SELECT e FROM Endpoint e JOIN e.device.account a WHERE a.identifier = :account_identifier")
public class Endpoint {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String arn;

    @OneToOne(cascade = CascadeType.PERSIST)
    /* This is only applicable in a test environment which auto generates the schema.
     * For some reason Hibernate (5.4.1.Final) logs an error message when trying to create a foreign key. 
     * The mvn test action completes succesfully, still the error detracts the value of the log.   
     */
    @JoinColumn(foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT)) 
    @NotNull
    Device device;
    
    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    private Instant modifiedAt;

    @Column(name="accessed_at")
    private Instant accessedAt;

    public Endpoint() {
        this.createdAt = Instant.now();    	
    }
    
    public Endpoint(String arn, Device device) {
		super();
		this.arn = arn;
		this.device = device;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
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
	
	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getAccessedAt() {
		return accessedAt;
	}

	public void setAccessedAt(Instant accessedAt) {
		this.accessedAt = accessedAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arn == null) ? 0 : arn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Device))
			return false;
		
		Endpoint endpoint = (Endpoint) that;
		
		return this.arn.equals(endpoint.arn);
	}
	
	@Override
	public String toString() {
		return String.format("{arn:%s}", this.arn);
	}
}
