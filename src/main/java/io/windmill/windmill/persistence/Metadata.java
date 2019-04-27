package io.windmill.windmill.persistence;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;
import io.windmill.windmill.web.common.Configuration;

@Entity
public class Metadata {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@NotNull
	private String target;
	
	@NotNull
	private String branch;
	
	@NotNull
	private String shortSha;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	private Configuration configuration;

    @NotNull
    private Instant certificateExpiryDate;

    @Column(name="created_at")
    @NotNull
    private Instant createdAt;
	
    @Column(name="modified_at")
    private Instant modifiedAt;

	public Metadata() {
		super();
		this.configuration = Configuration.RELEASE;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getBranch() {
		return branch;
	}
	
	public void setBranch(String branch) {
		this.branch = branch;
	}
	
	public String getShortSha() {
		return shortSha;
	}
	
	public void setShortSha(String shortSha) {
		this.shortSha = shortSha;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}		

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCertificateExpiryDate() {
		return certificateExpiryDate;
	}
	
	public void setCertificateExpiryDate(Instant certificateExpiryDate) {
		this.certificateExpiryDate = certificateExpiryDate;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;

	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Metadata))
			return false;
		
		Metadata build = (Metadata) that;
		
		return this.id.equals(build.id);
	}
}