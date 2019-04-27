package io.windmill.windmill.web.common;

public class Commit {

	private final String shortSha;
	private final String branch;

	public Commit(String shortSha, String branch) {
		super();
		this.shortSha = shortSha;
		this.branch = branch;
	}

	public String getShortSha() {
		return shortSha;
	}

	public String getBranch() {
		return branch;
	}	
}
