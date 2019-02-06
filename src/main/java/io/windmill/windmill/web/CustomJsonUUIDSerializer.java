package io.windmill.windmill.web;

import java.util.UUID;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class CustomJsonUUIDSerializer implements JsonbSerializer<UUID>
{

	@Override
	public void serialize(UUID obj, JsonGenerator generator, SerializationContext ctx) {
		generator.write(obj.toString());
		
	}

}