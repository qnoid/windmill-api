package io.windmill.windmill.persistence;

import javax.persistence.Query;
import javax.validation.constraints.NotNull;

@FunctionalInterface
public interface QueryConfiguration<T> {
		
	public @NotNull Query apply(Query query);

	public static <T> QueryConfiguration<T> identitifier(String identifier) {
		return query -> query.setParameter("identifier", identifier);			
	}
}