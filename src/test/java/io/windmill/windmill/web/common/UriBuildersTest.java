package io.windmill.windmill.web.common;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import junit.framework.Assert;

public class UriBuildersTest {

	@Test
	public void testGivenPathAssertURLString() throws URISyntaxException {
		URI uri = UriBuilders.create("/foo").build();
		
		Assert.assertEquals("https://ota.windmill.io/foo", uri.toString());
	}

}
