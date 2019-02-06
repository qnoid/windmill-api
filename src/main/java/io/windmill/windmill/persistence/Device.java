
package io.windmill.windmill.persistence;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQueries({
		@NamedQuery(name = "device.list", query = "SELECT d FROM Device d"),
        @NamedQuery(name = "device.with_account_identifier", query = "SELECT d FROM Device d WHERE d.account.identifier = :account_identifier"),
		@NamedQuery(name = "device.find_by_token", query = "SELECT d FROM Device d WHERE d.token = :token")})
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

    @Column(name="modified_at")
    @NotNull
    private Instant modifiedAt;

    @ManyToOne
    @NotNull
    Account account;
    
    /**
     * 
     */
    public Device()
    {
      // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     */
    public Device(String token, Account account)
    {
      this.token = token;
      this.account = account;
      this.createdAt = this.modifiedAt = Instant.now();
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

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant updatedAt) {
		this.modifiedAt = updatedAt;
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
