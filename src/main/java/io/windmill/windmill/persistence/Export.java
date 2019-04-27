
package io.windmill.windmill.persistence;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import io.windmill.windmill.web.CustomJsonUUIDSerializer;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;
import io.windmill.windmill.web.ManifestJsonbSerializer;
import io.windmill.windmill.web.MetadataJsonbSerializer;

@Entity
@NamedQuery(name = "export.with_account_identifier", query = "SELECT e FROM Export e WHERE e.account.identifier = :account_identifier ORDER BY e.title ASC")
@NamedQuery(name = "export.belongs_to_account_identifier", query = "SELECT e FROM Export e WHERE e.account.identifier = :account_identifier AND e.identifier = :identifier")
@NamedQuery(name = "export.find_by_identifier", query = "SELECT e FROM Export e WHERE e.identifier = :identifier")
@NamedQuery(name = "export.find_by_bundle", query = "SELECT e FROM Export e WHERE e.bundle = :bundle")
public class Export {
	
	public static class Manifest {
    	
    	final private String name = "manifest.plist";
        private Export export;
        
    	public Manifest(Export export) {
			this.export = export;
		}

    	/**
    	 * 
    	 * @param account TODO
    	 * @return the path where the manifest is actually available to download from
    	 */
		public URI path(Account account) {
			return this.getExport()
					.location(account)
    				.path(this.name)
    				.build();
    	}
		
		public URI path() {
			return this.path(this.export.account);
    	}

		public Export getExport() {
			return export;
		}

		public void setExport(Export export) {
			this.export = export;
		}
    }
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private UUID identifier;

    @NotNull
    private String bundle;

	@NotNull
    private Double version;

    @NotNull
    private String title;

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="accessed_at")
    private Instant accessedAt;

    @Column(name="modified_at")
    private Instant modifiedAt;

    @ManyToOne
    @JsonbTransient
    public Account account;
    
    @Transient
	private Manifest manifest;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @NotNull
    private Metadata metadata;

    /**
     * 
     */
    public Export()
    {
    	this.identifier = UUID.randomUUID();
    	this.account = new Account();
    	this.metadata = new Metadata();
        this.createdAt = Instant.now();
    }
    
    /**
     * 
     */
    public Export(String bundle, Double version, String title)
    {
    	this.identifier = UUID.randomUUID();    	
    	this.bundle = bundle;
    	this.version = version;
    	this.title = title;
    	this.createdAt = Instant.now();
    }
        
	private UriBuilder location(Account account) {
		return UriBuilder.fromPath(account.getIdentifier().toString())
				.path(this.getIdentifier().toString())
				.path("export");
	}

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	@JsonbTypeSerializer(CustomJsonUUIDSerializer.class)
	public @NotNull UUID getIdentifier() {
		return identifier;
	}

	public void setIdentifier(@NotNull UUID identifier) {
		this.identifier = identifier;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
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

	@JsonbTypeSerializer(MetadataJsonbSerializer.class)
	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public boolean hasAccount(Account that) {
		return Optional.ofNullable(this.account).filter(account -> account.equals(that)).isPresent();
	}

	public URI path(Account account) {
		return this.location(account)
				.path(String.format("%s.ipa", this.title))
				.build();
	}

	public URI path() {
		return this.path(this.account);
	}

	/**
	 * 
	 * @return a URL, pointing to the manifest where this Export can be installed from using the `itms-services://?action=download-manifest&url=` notation.
	 * the URL must be accessible in such a way that an iOS device can install it by simply issuing a GET to this URL.
	 * 
	 * No headers or query parameters should be required to install the Export using this URL. Neither is supported by iOS in its handling of `itms-services` links. 
	 */
	@JsonbProperty("url")
	@JsonbTypeSerializer(ManifestJsonbSerializer.class)
	public Manifest getManifest() {
		return new Manifest(this);
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
		return String.format("{identifier:%s, title:%s, version:%s}", this.bundle, this.title, this.version);
	}
}
