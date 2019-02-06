package io.windmill.windmill.persistence;

import javax.persistence.Query;
import javax.validation.constraints.NotNull;

@FunctionalInterface
public interface QueryConfiguration<T> {
		
	public @NotNull Query apply(Query query);

	public static <T, S> QueryConfiguration<T> identitifier(S identifier) {
		return query -> query.setParameter("identifier", identifier);			
	}
}