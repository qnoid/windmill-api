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
