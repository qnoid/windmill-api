package io.windmill.windmill.web.resources;

import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;

import io.windmill.windmill.web.JsonbAdapterUUIDToString;

public class AccountIdentifier {

	public UUID account_identifier;

	public AccountIdentifier() {
		super();
	}

	public AccountIdentifier(UUID account_identifier) {
		this.account_identifier = account_identifier;
	}

	@JsonbTypeAdapter(JsonbAdapterUUIDToString.class)
	public UUID getAccount_identifier() {
		return account_identifier;
	}

	public void setAccount_identifier(UUID account_identifier) {
		this.account_identifier = account_identifier;
	}

	@Override
	public String toString() {
		return this.account_identifier.toString();
	}
}
