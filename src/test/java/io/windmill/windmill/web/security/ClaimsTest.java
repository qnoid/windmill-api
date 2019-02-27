package io.windmill.windmill.web.security;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class ClaimsTest {

	@Test
	public void testGivenUUIDAssertIsSub() {

		Claims<?> claims = Claims.create();
		claims.sub("14810686-4690-4900-ada5-8b0b7338aa39");
		
		UUID actual = UUID.fromString("14810686-4690-4900-ada5-8b0b7338aa39");
		
		Assert.assertTrue(claims.isSub(actual, UUID::fromString));
	}

	@Test
	public void testGivenNullAssertIsSub() {

		Claims<?> claims = Claims.create();
		claims.sub("14810686-4690-4900-ada5-8b0b7338aa39");
		
		Assert.assertFalse(claims.isSub(null, UUID::fromString));
	}
}
