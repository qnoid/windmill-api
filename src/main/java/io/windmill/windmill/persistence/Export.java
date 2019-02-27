
package io.windmill.windmill.persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;
import io.windmill.windmill.web.common.UriBuilders;

@Entity
@NamedQueries({
        @NamedQuery(name = "export.with_account_identifier", query = "SELECT e FROM Export e WHERE e.account.identifier = :account_identifier ORDER BY e.title ASC"),
        @NamedQuery(name = "export.find_by_identifier", query = "SELECT e FROM Export e WHERE e.identifier = :identifier")})
public class Export {
	
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

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    private Instant modifiedAt;

    @ManyToOne
    @NotNull
    @JsonbTransient
    public Account account;
    
    @Transient
    @JsonbProperty("url")
	private String URL;
    
    /**
     * 
     */
    public Export()
    {
    	this.account = new Account();
        this.createdAt = Instant.now();
    }
    
    /**
     * 
     */
    public Export(String identifier, Double version, String title)
    {
      this.identifier = identifier;
      this.version = version;
      this.title = title;
      this.createdAt = Instant.now();
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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public boolean hasAccount(Account that) {
		return Optional.ofNullable(this.account).filter(account -> account.equals(that)).isPresent();
	}
	
	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getURL() {
		URI path = UriBuilder.fromPath(this.account.getIdentifier().toString())
				.path(this.identifier)
				.path(String.valueOf(this.version))
				.path(this.title)
				.build();
				
		try {
			return UriBuilders.createITMS(String.format("%s.plist", path));
		} catch (IllegalArgumentException | UriBuilderException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.identifier == null) ? 0 : this.identifier.hashCode());
		result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
		result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Export))
			return false;
		
		Export export = (Export) that;
		
		return this.identifier.equals(export.identifier) && 
				this.version.equals(export.version) && 
				this.title.equals(export.title);
	}
	
	@Override
	public String toString() {
		return String.format("{identifier:%s, title:%s, version:%s}", this.identifier, this.title, this.version);
	}
}
