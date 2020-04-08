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

package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public class FormDataMap {

	static public FormDataMap get(MultipartFormDataInput input) {
		return new FormDataMap(input.getFormDataMap());
	}
	
	private Map<String, List<InputPart>> map;

	public FormDataMap(Map<String, List<InputPart>> map) {
		super();
		this.map = map;
	}
	
	public InputStream read(String key) throws IllegalArgumentException {
		
		try {
			List<InputPart> value = this.map.get(key);
			
			return value.get(0).getBody(InputStream.class, null);
		}
		catch (NullPointerException | IOException e) {
			throw new IllegalArgumentException(String.format("Mandatory form parameter '%s' is missing.", key));
		}
	}		
}
