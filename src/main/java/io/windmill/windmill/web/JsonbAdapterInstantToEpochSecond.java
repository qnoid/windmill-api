package io.windmill.windmill.web;

import java.time.Instant;

import javax.json.bind.adapter.JsonbAdapter;

public class JsonbAdapterInstantToEpochSecond implements JsonbAdapter<Instant, Long>
{

	@Override
	public Long adaptToJson(Instant instant) throws Exception {
		return instant.getEpochSecond();
	}

	@Override
	public Instant adaptFromJson(Long epochSecond) throws Exception {
		return Instant.ofEpochSecond(epochSecond);
	}

}