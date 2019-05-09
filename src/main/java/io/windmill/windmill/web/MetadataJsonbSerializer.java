package io.windmill.windmill.web;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import io.windmill.windmill.persistence.Metadata;

public class MetadataJsonbSerializer implements JsonbSerializer<Metadata> {

	@Override
	public void serialize(Metadata metadata, JsonGenerator generator, SerializationContext ctx) {
						
		generator.writeStartObject()
			.write("configuration", metadata.getConfiguration().name().toUpperCase())
			.writeStartObject("commit")
				.write("branch", metadata.getBranch())
				.write("shortSha", metadata.getShortSha())
				.write("date", metadata.getDate().getEpochSecond())
			.writeEnd()
			.writeStartObject("applicationProperties")
				.write("bundleDisplayName", metadata.getBundleDisplayName())
				.write("bundleVersion", metadata.getBundleVersion())
			.writeEnd()
			.writeStartObject("deployment")
				.write("target", metadata.getTarget())
			.writeEnd()
			.writeStartObject("distributionSummary")
				.write("certificateExpiryDate", metadata.getCertificateExpiryDate().getEpochSecond())
			.writeEnd()
		.writeEnd();
	}
}