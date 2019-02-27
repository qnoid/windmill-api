package io.windmill.windmill.web;

import java.time.Instant;
import java.util.Optional;

import javax.json.bind.adapter.JsonbAdapter;

public class JsonbAdapterInstantToEpochSecond implements JsonbAdapter<Instant, Long>
{

	@Override
	public Long adaptToJson(Instant value) throws Exception {
		return Optional.ofNullable(value).map(instant -> instant.getEpochSecond()).orElse(null);
	}

	@Override
	public Instant adaptFromJson(Long value) throws Exception {
		return Optional.ofNullable(value).map(epochSecond -> Instant.ofEpochSecond(epochSecond)).orElse(null);
	}

}