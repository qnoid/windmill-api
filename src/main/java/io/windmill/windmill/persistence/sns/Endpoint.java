package io.windmill.windmill.persistence.sns;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Device;

@Entity
@Table(schema="sns")
@NamedQueries({
    @NamedQuery(name = "endpoint.find_by_device_token", query = "SELECT e FROM Endpoint e WHERE e.device.token = :device_token"),
    @NamedQuery(name = "endpoint.find_by_account_identifier", query = "SELECT e FROM Endpoint e JOIN e.device.account a WHERE a.identifier = :account_identifier")})
public class Endpoint {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String arn;

    @OneToOne(cascade = CascadeType.MERGE)    
    @NotNull
    Device device;
    
    public Endpoint() {
    	
    }
    
    public Endpoint(String arn, Device device) {
		super();
		this.arn = arn;
		this.device = device;
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
