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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public final class MustacheWriter {
	
    private final MustacheFactory mf = new DefaultMustacheFactory();

	private ByteArrayOutputStream parse(Reader reader, Map<String, Object> scopes) throws IOException {		
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final Writer writer = new OutputStreamWriter(out);
	    final Mustache mustache = this.mf.compile(reader, "plist");
	    mustache.execute(writer, scopes);
	    writer.flush();
	
	    return out;
	}
	
	public ByteArrayOutputStream substitute(ByteArrayOutputStream buffer, Map<String, Object> substitutions) throws IOException {
		
		try {
			InputStream is = new ByteArrayInputStream(buffer.toByteArray());
			InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));

			return parse(reader, substitutions);		
		} catch (IOException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	public ByteArrayOutputStream substitute(String value, Map<String, Object> substitutions) throws IOException {
		return parse(new StringReader(value), substitutions);
	}
}