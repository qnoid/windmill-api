package io.windmill.windmill.web.security;

import java.util.Optional;

public enum MacAlgorithm {
	HMAC_SHA256("HmacSHA256", "HS256");
	
	final String system;
	final String canonical;

	MacAlgorithm(String system, String canonical) {
		this.system = system;
		this.canonical = canonical;
	}
	
	public static Optional<MacAlgorithm> of(String algorithm) {
        for (MacAlgorithm a : MacAlgorithm.values()) {
            if (a.system.equals(algorithm) || a.canonical.equals(algorithm)) {
                return Optional.of(a);
            }
        }

		return Optional.empty();
	}
    
    @Override
    public String toString() {
        return this.canonical;
    }
}