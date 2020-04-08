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

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;

public class Manifest {

	public static Manifest manifest(InputStream in) throws IOException, ConfigurationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	
		return Manifest.manifest(out);
	}

	public static Manifest manifest(ByteArrayOutputStream buffer) throws ConfigurationException {
				
		InputStream is = new ByteArrayInputStream(buffer.toByteArray()); 
		InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
		XMLPropertyListConfiguration xmlPropertyListConfiguration = new XMLPropertyListConfiguration();			
		xmlPropertyListConfiguration.read(new BufferedReader(reader));
		XMLPropertyListConfiguration items = xmlPropertyListConfiguration.get(XMLPropertyListConfiguration.class, "items");

		String bundle_identifier = items.getString("metadata.bundle-identifier");
        String bundle_version = items.getString("metadata.bundle-version");
        String bundle_title = items.getString("metadata.title");

		return new Manifest(buffer, bundle_identifier, bundle_version, bundle_title);
	}
	
	private ByteArrayOutputStream buffer;

    @NotNull
    private String bundle;

	@NotNull
    private String version;

    @NotNull
	private String title;
    
	public Manifest(ByteArrayOutputStream buffer, String bundle, String version, String title) {
		this.buffer = buffer;
		this.bundle = bundle;
		this.version = version;
		this.title = title;
	}
	
	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}

	public String getBundle() {
		return this.bundle;
	}

	public String getVersion() {
		return this.version;
	}

	public String getTitle() {
		return this.title;
	}	
}
