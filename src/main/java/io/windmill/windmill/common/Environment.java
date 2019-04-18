package io.windmill.windmill.common;

import java.util.Optional;

public enum Environment {
	INSTANCE;

	boolean isProduction() {
		return Optional.ofNullable(System.getenv("WINDMILL_ENVIRONMENT")).isPresent();		
	}
}
