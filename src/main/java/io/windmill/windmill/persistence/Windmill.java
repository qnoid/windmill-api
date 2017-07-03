
package io.windmill.windmill.persistence;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.windmill.windmill.web.CustomJsonInstantSerializer;

@Entity
@NamedQueries({
        @NamedQuery(name = "windmill.with_account_identifier", query = "SELECT w FROM Windmill w WHERE w.account.identifier = :account_identifier"),
        @NamedQuery(name = "windmill.find_by_identifier", query = "SELECT w FROM Windmill w WHERE w.identifier = :identifier")})
public class Windmill {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String identifier;

	@NotNull
    private Double version;

    @NotNull
    private String title;

    @Column(name="updated_at")
    @NotNull
    private Instant updatedAt;

    @ManyToOne
    @NotNull
    Account account;
    
    @SuppressWarnings("unused")
	transient private String URL;
    
    /**
     * 
     */
    public Windmill()
    {
      // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     */
    public Windmill(String identifier, Double version, String title)
    {
      this.identifier = identifier;
      this.version = version;
      this.title = title;
      this.updatedAt = Instant.now();
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

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}	
	
    @JsonSerialize(using=CustomJsonInstantSerializer.class)
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public boolean account(String accountIdentifier) {
		return this.account.getIdentifier().equals(accountIdentifier);
	}
	
	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getURL() {
		//"itms-services://?action=download-manifest&url=https://ota.windmill.io/{user_identifier}/{windmill_identifier}/{windmill_version}/{windmill_title}.plist"
		final String ITMS_SERVICES_URL_STRING = "itms-services://?action=download-manifest&url=https://ota.windmill.io/%s/%s/%s/%s.plist";
		
		return String.format(ITMS_SERVICES_URL_STRING, 
				this.account.getIdentifier(),
				this.identifier,
				this.version,
				this.title);
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
		
		if (!(that instanceof Windmill))
			return false;
		
		Windmill windmill = (Windmill) that;
		
		return this.identifier.equals(windmill.identifier);
	}
	
	@Override
	public String toString() {
		return String.format("{identifier:%s, title:%s, version:%s}", this.identifier, this.title, this.version);
	}
}
