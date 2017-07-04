package io.windmill.windmill.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

public class WindmillMetadataTest {

	@Test(expected = ConfigurationException.class)
	public void testGivenMalformedContentAssertConfigurationExceptionThrown() throws ConfigurationException {
		
		byte[] data = "malformed content type".getBytes();
		InputStream in = new ByteArrayInputStream(data);
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		
		WindmillMetadata.read(inputStreamReader);
	}

}
