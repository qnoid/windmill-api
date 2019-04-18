package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.services.exceptions.ExportGoneException;
import io.windmill.windmill.services.exceptions.NoExportException;

@ApplicationScoped
public class ExportService {

	@Inject
    WindmillEntityManager entityManager;

	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public Export get(UUID export_identifier) throws ExportGoneException {		
				
		try {
			return this.entityManager.getSingleResult("export.find_by_identifier", identitifier(export_identifier));	
		} catch (NoResultException e) {
			throw new ExportGoneException(ExportGoneException.EXPORT_NOT_FOUND, e);
		}		
	}

	public Export belongs(UUID account_identifier, UUID export_identifier) {
		try {
			
			Export export = this.entityManager.getSingleResult("export.belongs_to_account_identifier", new QueryConfiguration<Export>() {
		
				@Override
				public @NotNull Query apply(Query query) {
					query.setParameter("identifier", export_identifier);
					query.setParameter("account_identifier", account_identifier);
					
					return query;
				}
			});
			
			return export;
		} catch (NoResultException e) {
			throw new NoExportException("An export does not exist for the given account.");
		}
	}    
}
