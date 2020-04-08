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

import javax.json.bind.annotation.JsonbProperty;

/**
 * A user record in Apple's the `CloudKit` 
 */
public class CKUserRecord {
	
	@JsonbProperty("user_identifier")
    private String identifier;    
	
	@JsonbProperty("user_container")
    private String container;    
    
	public CKUserRecord() {
		super();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof CKUserRecord))
			return false;
		
		CKUserRecord userRecord = (CKUserRecord) that;
		
		return this.identifier.equals(userRecord.identifier) && 
				this.container.equals(userRecord.container);
	}
	
	@Override
	public String toString() {
		return String.format("{identifier:%s, container:%s}", this.identifier, this.container);
	}
}
