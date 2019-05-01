package io.windmill.windmill.web.common;

public class ApplicationProperties {

	private final String bundleDisplayName;
	private final String bundleVersion;

	public ApplicationProperties(String bundleDisplayName, String bundleVersion) {
		this.bundleDisplayName = bundleDisplayName;
		this.bundleVersion = bundleVersion;
	}

	public String getBundleDisplayName() {
		return bundleDisplayName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}
}
