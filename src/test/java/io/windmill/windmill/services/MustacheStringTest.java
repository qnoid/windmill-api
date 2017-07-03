package io.windmill.windmill.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import junit.framework.Assert;

public class MustacheStringTest {

	@Test
	public void testGivenSamplePlistAssertSubstitutions() throws IOException {
		String expected = IOUtils.toString(this.getClass().getResourceAsStream("/expected.plist"), "UTF-8");
		
		InputStream plistStream = this.getClass().getResourceAsStream("/sample.plist");
		
		String plist = IOUtils.toString(plistStream, "UTF-8");
		
		ByteArrayOutputStream actual = new MustacheWriter().urlString(plist, String.format("https://%s/%s.ipa", StorageService.BUCKET_CANONICAL_NAME, "foo"));
		
		Assert.assertEquals(expected, new String( actual.toByteArray(), "UTF-8"));
	}

}
