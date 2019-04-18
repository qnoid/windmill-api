package io.windmill.windmill.web.common;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import io.windmill.windmill.web.Host;
import junit.framework.Assert;

public class UriBuildersTest {

	@Test
	public void testGivenPathAssertURLString() throws URISyntaxException {
		URI uri = Host.OTA_DOMAIN.builder().path("/foo").build();
		
		Assert.assertEquals("https://ota.windmill.io/foo", uri.toString());
	}

}
