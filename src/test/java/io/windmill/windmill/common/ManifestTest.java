//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

package io.windmill.windmill.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import io.windmill.windmill.services.MustacheWriter;
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

		Map<String, Object> substitutions = new HashMap<>();
		substitutions.put("URL", "https://server.windmill.io/foo.ipa");
		
		ByteArrayOutputStream outputStream = new MustacheWriter().substitute(manifest.getBuffer(), substitutions);
		
		String actual = new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
		Assert.assertEquals(expected, actual);
	}

}
