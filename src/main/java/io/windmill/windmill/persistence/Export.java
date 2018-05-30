
package io.windmill.windmill.persistence;

import java.net.URI;
import java.net.URISyntaxException;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.windmill.windmill.web.CustomJsonInstantSerializer;
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
    @NotNull
    private Instant modifiedAt;

    @ManyToOne
    @NotNull
    Account account;
    
    @SuppressWarnings("unused")
	transient private String URL;
    
    /**
     * 
     */
    public Export()
    {
      // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     */
    public Export(String identifier, Double version, String title)
    {
      this.identifier = identifier;
      this.version = version;
      this.title = title;
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
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	
    @JsonSerialize(using=CustomJsonInstantSerializer.class)
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant updatedAt) {
		this.modifiedAt = updatedAt;
	}

	public boolean account(String accountIdentifier) {
		return this.account != null && this.account.getIdentifier().equals(accountIdentifier);
	}
	
	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getURL() {
		URI path = UriBuilder.fromPath(this.account.getIdentifier())
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
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Export))
			return false;
		
		Export export = (Export) that;
		
		return this.identifier.equals(export.identifier);
	}
	
	@Override
	public String toString() {
		return String.format("{identifier:%s, title:%s, version:%s}", this.identifier, this.title, this.version);
	}
}
