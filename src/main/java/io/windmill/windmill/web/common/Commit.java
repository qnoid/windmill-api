package io.windmill.windmill.web.common;

import java.time.Instant;

public class Commit {

	private final String shortSha;
	private final String branch;
	private final Instant date;

	public Commit(String shortSha, String branch, Instant date) {
		super();
		this.shortSha = shortSha;
		this.branch = branch;
		this.date = date;
	}

	public String getShortSha() {
		return shortSha;
	}

	public String getBranch() {
		return branch;
	}

	public Instant getDate() {
		return date;
	}	
	
}
