
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

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQuery(name = "device.list", query = "SELECT d FROM Device d")
@NamedQuery(name = "device.with_account_identifier", query = "SELECT d FROM Device d WHERE d.account.identifier = :account_identifier")
@NamedQuery(name = "device.find_by_token", query = "SELECT d FROM Device d WHERE d.token = :token")
public class Device {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String token;

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @NotNull
    Account account;
    
    /**
     * 
     */
    public Device()
    {
        this.createdAt = Instant.now();
    }
    
    /**
     * 
     */
    public Device(String token, Account account)
    {
      this.token = token;
      this.account = account;
      this.createdAt = Instant.now();
    }
        
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
    public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Device))
			return false;
		
		Device device = (Device) that;
		
		return this.token.equals(device.token);
	}
	
	@Override
	public String toString() {
		return String.format("{token:%s}", this.token);
	}
}
