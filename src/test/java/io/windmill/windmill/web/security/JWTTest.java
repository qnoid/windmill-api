package io.windmill.windmill.web.security;

import org.junit.Test;

import io.windmill.windmill.web.security.JWT.Header;
import io.windmill.windmill.web.security.JWT.Header.Type;
import junit.framework.Assert;

public class JWTTest {

	@Test
	public void testGivenValidTypeAssertType() {

		String typ = "JWT";
		Header.Type actual = Header.Type.of(typ).orElse(null);
		
		Assert.assertEquals(Type.JWT, actual);
	}

}
