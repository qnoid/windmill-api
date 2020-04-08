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
