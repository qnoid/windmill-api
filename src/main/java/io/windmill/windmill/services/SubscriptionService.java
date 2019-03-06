package io.windmill.windmill.services;

import static io.windmill.windmill.common.Condition.guard;
import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.jboss.logging.Logger;

import io.windmill.windmill.apple.AppStoreConnect.Bundle;
import io.windmill.windmill.apple.AppStoreConnect.Product;
import io.windmill.windmill.persistence.Extended;
import io.windmill.windmill.persistence.Provider;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Metadata;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.persistence.web.Receipt;
import io.windmill.windmill.services.AppStoreService.InAppPurchaseReceipt;
import io.windmill.windmill.services.AppStoreService.LatestReceipt;

@RequestScoped
public class SubscriptionService {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionService.class);
	
    @Inject @Extended
    private WindmillEntityManager entityManager;

    @Inject
    private AppStoreService appStoreService;
			
    public SubscriptionService() {
		super();
	}

	public SubscriptionService(WindmillEntityManager entityManager, AppStoreService appStoreService) {
		this.entityManager = entityManager;
		this.appStoreService = appStoreService;
	}

	@PostConstruct
    private void init() {
    	this.entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }
    
	/**
	 * 
	 * @param transactions an array of transactions as returned by the {@link AppStoreService}
	 * @return the transaction that expires last in the given array
	 * @throws ReceiptVerificationException
	 */
	/* private */ JsonObject latest(JsonArray transactions) throws ReceiptVerificationException {
		return transactions.stream().filter(Product.KNOWN_PRODUCT)
				.sorted(AppStoreService.EXPIRES_DATE_DESCENDING)
				.findFirst()
				.orElseThrow(new Supplier<ReceiptVerificationException>() {

					@Override
					public ReceiptVerificationException get() {
						return new ReceiptVerificationException(String.format("Receipt was valid but no transactions found with a known product id."));
					}
				}).asJsonObject();
	}
	
	/* private */ Subscription updateOrCreateSubscription(String transaction_identifier, String receipt, Instant expiresAt, Map<Metadata, Boolean> metadata) {
		
		AppStoreTransaction appStoreTransaction = this.entityManager.findOrProvide("transaction.find_by_identifier", identitifier(transaction_identifier), new Provider<AppStoreTransaction>(){

			@Override
			public AppStoreTransaction get() {
				AppStoreTransaction appStoreTransaction = new AppStoreTransaction(transaction_identifier, receipt, expiresAt, new Subscription());
				metadata.put(Metadata.WAS_CREATED, true);
				entityManager.persist(appStoreTransaction);
				return appStoreTransaction;
			}			
		});		
		
		appStoreTransaction.setReceipt(receipt);
		appStoreTransaction.setExpiresAt(expiresAt);
		appStoreTransaction.setModifiedAt(Instant.now());
		
		return appStoreTransaction.getSubscription();				
	}
	
	/* private */ Subscription updateOrCreateSubscription(JsonArray transactions, String receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException {
		
		if (transactions.isEmpty()) {
			throw new NoRecoredTransactionsException("The App Store has not recorded any transactions for the user yet. It may be that the application receipt has not yet been updated.");
		}			

		JsonObject transaction = latest(transactions);
		
		String transaction_identifier = transaction.getString("original_transaction_id");
		String expiresAt = transaction.getString("expires_date");
			
		return updateOrCreateSubscription(transaction_identifier, receipt, Instant.from(AppStoreService.DATE_FORMATER.parse(expiresAt)), metadata);
	}
	
	/* private */ Subscription updateSubscription(String transaction_identifier, String receipt, Instant expiresAt, Map<Metadata, Boolean> metadata) throws NoSubscriptionException {
		
		try {
			AppStoreTransaction appStoreTransaction = this.entityManager.getSingleResult("transaction.find_by_identifier", identitifier(transaction_identifier));	
		
			appStoreTransaction.setReceipt(receipt);
			appStoreTransaction.setExpiresAt(expiresAt); //should it update only when its the later than the current or not?
			appStoreTransaction.setModifiedAt(Instant.now());
		
			return appStoreTransaction.getSubscription();
		} catch (NoResultException e) {
			throw new NoSubscriptionException(String.format("A subscription does not exist for the given identifier '%s'.", transaction_identifier));
		}
	}

	Subscription updateSubscription(JsonArray transactions, String receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException, NoSubscriptionException {
		
		if (transactions.isEmpty()) {
			throw new NoRecoredTransactionsException("The App Store has not recorded any transactions for the user yet. It may be that the application receipt has not yet been updated.");
		}			

		JsonObject transaction = latest(transactions);
		
		String transaction_identifier = transaction.getString("original_transaction_id");
		String expiresAt = transaction.getString("expires_date");
			
		return updateSubscription(transaction_identifier, receipt, Instant.from(AppStoreService.DATE_FORMATER.parse(expiresAt)), metadata);
	}
	
	public Subscription subscription(Receipt receipt)  throws ReceiptVerificationException, NoRecoredTransactionsException {
		return this.subscription(receipt, new Hashtable<>());
	}

	public Subscription subscription(Receipt receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException {
		
		String receiptData = receipt.getData();		
		
		return this.appStoreService.receipt(receiptData, new InAppPurchaseReceipt() {
			
			@Override
			public AppStoreTransaction process(String bundle_id, JsonArray transactions)
					throws ReceiptVerificationException, NoRecoredTransactionsException {
				
				if (Bundle.of(bundle_id).isPresent()) {							
					return updateOrCreateSubscription(transactions, receiptData, metadata).getTransaction();
				} else {
					throw new ReceiptVerificationException(String.format("Receipt was valid but there was mismatch on the bundle id '%s'.", bundle_id));
				}
			}
		}).getSubscription();
	}
	
	/**
	 * 
	 * @param subscription the subscription should exist
	 * @throws ReceiptVerificationException
	 * @throws NoRecoredTransactionsException
	 * @throws SubscriptionExpiredException if the subscription has since elapsed
	 * @throws NoSubscriptionException if the given subscription does not exist
	 */
	public void latest(Subscription subscription)  throws ReceiptVerificationException, NoRecoredTransactionsException, SubscriptionExpiredException, NoSubscriptionException {
	
		String receipt = subscription.getTransaction().getReceipt();
		
		this.latest(new Receipt(receipt), new Hashtable<>());
	}
	public void latest(Receipt receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException, SubscriptionExpiredException, NoSubscriptionException {
		
		String receiptData = receipt.getData();		
		
		Subscription subscription = this.appStoreService.latest(receiptData, new LatestReceipt() {
			
			@Override
			public AppStoreTransaction process(JsonArray transactions) throws ReceiptVerificationException, NoRecoredTransactionsException {
				return updateSubscription(transactions, receiptData, metadata).getTransaction();
			}
		}).getSubscription();
		
		guard(subscription.isActive(), () -> new SubscriptionExpiredException());
	}

	public Subscription subscription(UUID account_identifier, UUID subscription_identifier) throws NoSubscriptionException {
		
		try {
			
			Subscription subscription = this.entityManager.getSingleResult("subscription.belongs_to_account_identifier", new QueryConfiguration<Subscription>() {

				@Override
				public @NotNull Query apply(Query query) {
					query.setParameter("identifier", subscription_identifier);
					query.setParameter("account_identifier", account_identifier);				
					return query;
				}
			});
			
			return subscription;
		} catch (NoResultException e) {
			throw new NoSubscriptionException("A subscription does not exist for the given account.");
		}		
	}
	
	public Subscription subscription(UUID subscription_identifier) throws NoSubscriptionException {
		
		try {
			
			return this.entityManager.getSingleResult("subscription.find_by_identifier", identitifier(subscription_identifier));
		} catch (NoResultException e) {
			throw new NoSubscriptionException("A subscription does not exist for the given account.");
		}		
	}
}
