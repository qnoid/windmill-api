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

package io.windmill.windmill.persistence;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import junit.framework.Assert;

public class ExportTest {

	@Test
	public void testGivenManifestAssertURIPath() {
		final UUID account_identifier = UUID.fromString("7b4b8a85-a49e-4a48-a4a9-d3423384ce71");
		final UUID export_identifier = UUID.fromString("18658c6f-91aa-4fe6-a9ec-70f349057aa5");
		
		
		Export export = new Export("bundle", "1.0", "title");
		export.setIdentifier(export_identifier);
		
		export.account = new Account(account_identifier);
		
		URI manifest = export.getManifest().path();
		
		//{account_identifier}/{export_identifier}/export/manifest.plist
		Assert.assertEquals(account_identifier + "/" + export_identifier + "/export/manifest.plist", manifest.toString());
	}

}
