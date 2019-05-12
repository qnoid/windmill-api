package io.windmill.windmill.web;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import com.amazonaws.Protocol;

import io.windmill.windmill.persistence.Account;

public enum Host {
	API_PRODUCTION("api.windmill.io"),
	API_DEVELOPMENT("192.168.1.2"),
	
	SERVER_PRODUCTION("server.windmill.io");
			
	@FunctionalInterface
	public static interface URITemplate {
		public UriBuilder apply(UriBuilder builder);
	}

	public static final Host SERVER_DOMAIN = SERVER_PRODUCTION;
	public static final Host API_DOMAIN = API_PRODUCTION;

	private String domain;
	
	private Host(String domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return domain;
	}

	public UriBuilder account(Account account) {
		return this.builder().path("account").path(account.getIdentifier().toString());
	}

	public UriBuilder builder() {
		return this.builder(Protocol.HTTPS);
	}
	
	public UriBuilder builder(@NotNull Protocol protocol) {
		try {
			return UriBuilder.fromUri(new URI(protocol.toString(), this.domain, null, null));
		} catch (URISyntaxException e) {				
			return null;
		}
	}
}