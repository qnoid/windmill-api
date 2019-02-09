
package io.windmill.windmill.persistence;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.web.CustomJsonUUIDSerializer;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@NamedQueries({
    @NamedQuery(name = "account.list", query = "SELECT a FROM Account a"),
    @NamedQuery(name = "account.find_by_identifier", query = "SELECT a FROM Account a WHERE a.identifier = :identifier")})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private UUID identifier;

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    @NotNull
    private Instant modifiedAt;
    
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy="account")
    private Set<Export> exports;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy="account")
    private Set<Device> devices;
    
    /**
     * 
     */
    public Account()
    {
    	this.identifier = UUID.randomUUID();
	    this.createdAt = this.modifiedAt = Instant.now();		
		this.exports = new HashSet<Export>();
		this.devices = new HashSet<Device>();
    }
        
	public Account(UUID identifier) {
		this.identifier = identifier;
	    this.createdAt = this.modifiedAt = Instant.now();		
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

	public void setModifiedAt(Instant updatedAt) {
		this.modifiedAt = updatedAt;
	}
	
	public void add(Export export) {
		export.account = this;		
		this.exports.add(export);
	}
	
	public void add(Device device) {
		device.account = this;		
		this.devices.add(device);
	}    

}
