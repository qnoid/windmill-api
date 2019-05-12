package io.windmill.windmill.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import junit.framework.Assert;

public class MustacheStringTest {

	@Test
	public void testGivenSamplePlistAssertSubstitutions() throws IOException {
		String expected = IOUtils.toString(this.getClass().getResourceAsStream("/expected.plist"), "UTF-8");
		
		InputStream plistStream = this.getClass().getResourceAsStream("/sample.plist");
		
		String plist = IOUtils.toString(plistStream, "UTF-8");
		
		Map<String, Object> substitutions = new HashMap<>();
		substitutions.put("URL", String.format("https://%s/%s.ipa", "server.windmill.io", "foo"));

		ByteArrayOutputStream actual = new MustacheWriter().substitute(plist, substitutions);
		
		Assert.assertEquals(expected, new String( actual.toByteArray(), "UTF-8"));
	}

}
