package io.windmill.windmill.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import junit.framework.Assert;

public class MetadataTest {

	@Test(expected = ConfigurationException.class)
	public void testGivenMalformedContentAssertConfigurationExceptionThrown() throws ConfigurationException {
		
		byte[] data = "malformed content type".getBytes();
		InputStream in = new ByteArrayInputStream(data);
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		
		Manifest.read(inputStreamReader);
	}

	@Test
	public void testGivenPlistAssertReadMetadataSucceeeds() throws ConfigurationException {
		
		InputStream in = this.getClass().getResourceAsStream("/sample.plist");
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		
		Manifest metadata = Manifest.read(inputStreamReader);
		
		Assert.assertEquals("io.windmill.windmill", metadata.getBundle());
		Assert.assertEquals(1.0, metadata.getVersion());
		Assert.assertEquals("windmill", metadata.getTitle());
	}
}
