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

	@Column(name="deployment_target")
	@NotNull
	private String deploymentTarget;
	
	@Column(name="commit_branch")
	@NotNull
	private String commitBranch;
	
	@Column(name="commit_shortSha")
	@NotNull
	private String commitShortSha;
	
	@Column(name="commit_date")
	@NotNull
	private Instant commitDate;
	
	@Column(name="build_configuration")
	@Enumerated(EnumType.STRING)
	@NotNull
	private Configuration configuration;

	@Column(name="distribution_summary_certificate_expiry_date")
    @NotNull
    private Instant certificateExpiryDate;

	@Column(name="application_properties_bundle_display_name")
	@NotNull
	private String bundleDisplayName;

	@Column(name="application_properties_bundle_version")
	@NotNull
	private String bundleVersion;


	public Metadata() {
		super();
		this.configuration = Configuration.RELEASE;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTarget() {
		return deploymentTarget;
	}
	
	public void setTarget(String target) {
		this.deploymentTarget = target;
	}
	
	public String getBranch() {
		return commitBranch;
	}
	
	public void setBranch(String branch) {
		this.commitBranch = branch;
	}
	
	public String getShortSha() {
		return commitShortSha;
	}
	
	public void setShortSha(String shortSha) {
		this.commitShortSha = shortSha;
	}
	
	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getDate() {
		return commitDate;
	}
	
	public void setDate(Instant date) {
		this.commitDate = date;		
	}

	
	public String getBundleDisplayName() {
		return bundleDisplayName;
	}

	public void setBundleDisplayName(String bundleDisplayName) {
		this.bundleDisplayName = bundleDisplayName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String bundleVersion) {
		this.bundleVersion = bundleVersion;		
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