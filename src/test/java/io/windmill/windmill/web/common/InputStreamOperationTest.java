package io.windmill.windmill.web.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class InputStreamOperationTest {

	@Test
	public void testGivenTemplatePlistAssertURLStringReplace() {
		InputStream inputStream = this.getClass().getResourceAsStream("/expected.plist");
		String expected = new BufferedReader(new InputStreamReader(inputStream))
				  .lines().collect(Collectors.joining("\n"));

		InputStream in = this.getClass().getResourceAsStream("/sample.plist");

		ByteArrayOutputStream outputStream = InputStreamOperations.replaceURL("https://ota.windmill.io/foo.ipa").apply(in);
		
		String actual = new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
		Assert.assertEquals(expected, actual);
	}

}
