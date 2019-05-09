package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Fetch;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.services.exceptions.AccountServiceException;
import io.windmill.windmill.services.exceptions.NoAccountException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;

@ApplicationScoped
public class AccountService {

    @Inject
    WindmillEntityManager entityManager;

    @Inject
	NotificationService notificationService;
        
    public AccountService() {
		super();
	}

	public AccountService(WindmillEntityManager entityManager, NotificationService notificationService) {
		this.entityManager = entityManager;
		this.notificationService = notificationService;
	}

	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }
	
	private Account belongs(UUID account_identifier, UUID subscription_identifier, QueryConfiguration<Subscription> queryConfiguration) throws NoSubscriptionException {
		
		try {
						
			Subscription subscription = this.entityManager.getSingleResult("subscription.belongs_to_account_identifier", new QueryConfiguration<Subscription>() {
		
				@Override
				public @NotNull Query apply(Query query) {
					query.setParameter("identifier", subscription_identifier);
					query.setParameter("account_identifier", account_identifier);

					return queryConfiguration.apply(query);
				}
			});
			
    		subscription.setAccessedAt(Instant.now());

			return subscription.getAccount();
		} catch (NoResultException e) {
			throw new NoSubscriptionException("A subscription does not exist for the given account.");
		}
	}

	public Account get(UUID account_identifier) throws NoAccountException {
		
		try {
			return this.entityManager.getSingleResult("account.find_by_identifier", identitifier(account_identifier));
		} catch (NoResultException e){
    		throw new NoAccountException("An account for the given identifier doesn't exist. You need an active subscription.", e);
		}
	}    

	public Account belongs(UUID account_identifier, UUID subscription_identifier, Fetch fetch) throws NoSubscriptionException {
		return belongs(account_identifier, subscription_identifier, new QueryConfiguration<Subscription>() {

			@Override
			public @NotNull Query apply(Query query) {
				EntityGraph<Subscription> graph = entityManager.getEntityGraph(fetch.getEntityGraph());				
				return query.setHint("javax.persistence.fetchgraph", graph);
			}
		});		
	}
	
	public Account belongs(UUID account_identifier, UUID subscription_identifier) throws NoSubscriptionException {
		return belongs(account_identifier, subscription_identifier, QueryConfiguration.empty());		
	}
	
	public Export updateOrCreate(Account account, String export_bundle, String export_title,
			String export_version) throws AccountServiceException, NoAccountException {

		try {
			Export export = this.entityManager.findOrProvide("export.find_by_bundle", query -> 
					query.setParameter("bundle", export_bundle), () -> new Export(export_bundle, export_version, export_title));
		
			Condition.guard(export.account == null || export.hasAccount(account), 
					() -> new AccountServiceException("io.windmill.api: error: The bundle identifier is already used by another account."));
			
			export.setBundle(export_bundle);
			export.setTitle(export_title);
			export.setVersion(export_version);
			
			this.entityManager.persist(export);
			
			return export;		
		} catch (NoResultException e) {
			throw new NoAccountException("An account for the given identifier doesn't exist. You need an active subscription.");
		}		
	}
}