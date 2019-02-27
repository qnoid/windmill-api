package io.windmill.windmill.web;

import java.util.Optional;
import java.util.UUID;

import javax.json.bind.adapter.JsonbAdapter;

public class JsonbAdapterUUIDToString implements JsonbAdapter<UUID, String> {

	@Override
	public String adaptToJson(UUID value) throws Exception {
		return Optional.ofNullable(value).map(uuid -> uuid.toString()).orElse(null);
	}

	@Override
	public UUID adaptFromJson(String value) throws Exception {
		return Optional.ofNullable(value).map(name -> UUID.fromString(name)).orElse(null);
	}
}
