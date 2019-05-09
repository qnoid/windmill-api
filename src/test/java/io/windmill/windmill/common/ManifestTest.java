package io.windmill.windmill.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import junit.framework.Assert;

public class ManifestTest {

	@Test(expected = ConfigurationException.class)
	public void testGivenMalformedContentAssertConfigurationExceptionThrown() throws ConfigurationException, IOException {
		
		byte[] data = "malformed content type".getBytes();
		InputStream in = new ByteArrayInputStream(data);
		
		Manifest.manifest(in);
	}

	@Test
	public void testGivenPlistAssertReadMetadataSucceeeds() throws ConfigurationException, IOException {
		
		InputStream in = this.getClass().getResourceAsStream("/sample.plist");
		
		Manifest metadata = Manifest.manifest(in);
		
		Assert.assertEquals("io.windmill.windmill", metadata.getBundle());
		Assert.assertEquals("1.0", metadata.getVersion());
		Assert.assertEquals("windmill", metadata.getTitle());
	}
	
	@Test
	public void testGivenTemplatePlistAssertURLStringReplace() throws IllegalArgumentException, ConfigurationException, IOException {
		InputStream inputStream = this.getClass().getResourceAsStream("/expected.plist");
		String expected = new BufferedReader(new InputStreamReader(inputStream))
				  .lines().collect(Collectors.joining("\n"));

		InputStream in = this.getClass().getResourceAsStream("/sample.plist");
		Manifest manifest = Manifest.manifest(in);

		ByteArrayOutputStream outputStream = manifest.plistWithURLString("https://ota.windmill.io/foo.ipa");
		
		String actual = new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
		Assert.assertEquals(expected, actual);
	}

}
