package io.windmill.windmill.web.common;

public class Build {

	private final Configuration configuration;
	private final Commit commit;
	private final Deployment deployment;
	private final DistributionSummary distributionSummary;

	public Build(Configuration configuration, Commit commit, Deployment deployment, DistributionSummary distributionSummary) 
	{
		this.configuration = configuration;
		this.commit = commit;
		this.deployment = deployment;
		this.distributionSummary = distributionSummary;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Commit getCommit() {
		return commit;
	}

	public Deployment getDeployment() {
		return deployment;
	}

	public DistributionSummary getDistributionSummary() {
		return distributionSummary;
	}
}
