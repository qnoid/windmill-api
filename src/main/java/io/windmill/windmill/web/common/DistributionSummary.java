package io.windmill.windmill.web.common;

import java.time.Instant;

public class DistributionSummary {

	private final Instant certificateExpiryDate;

	public DistributionSummary(Instant certificateExpiryDate) {
		super();
		this.certificateExpiryDate = certificateExpiryDate;
	}

	public Instant getCertificateExpiryDate() {
		return certificateExpiryDate;
	}
	
	
}
