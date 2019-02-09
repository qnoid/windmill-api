package io.windmill.windmill.services;

import static io.windmill.windmill.persistence.QueryConfiguration.identitifier;

import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.logging.Logger;

import io.windmill.windmill.apple.AppStoreConnect.Bundle;
import io.windmill.windmill.apple.AppStoreConnect.Product;
import io.windmill.windmill.persistence.Provider;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Metadata;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.persistence.web.Receipt;
import io.windmill.windmill.services.AppStoreService.VerifyResponse;

@ApplicationScoped
public class SubscriptionService {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionService.class);
	
    @Inject
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
    
	/* private */ AppStoreTransaction updateOrCreateSubscription(String transaction_identifier, String receipt, Instant expiresAt, Map<Metadata, Boolean> metadata) {
		
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
		
		return appStoreTransaction;				
	}
	
	/* private */ AppStoreTransaction updateOrCreateSubscription(JsonArray transactions, String receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException {
		
		if (transactions.isEmpty()) {
			throw new NoRecoredTransactionsException("The App Store has not recorded any transactions for the user yet. It may be that the application receipt has not yet been updated.");
		}			

		JsonObject transaction = transactions.stream().filter(Product.KNOWN_PRODUCT)
				.sorted(VerifyResponse.EXPIRES_DATE_DESCENDING)
				.findFirst()
				.orElseThrow(new Supplier<ReceiptVerificationException>() {

					@Override
					public ReceiptVerificationException get() {
						return new ReceiptVerificationException(String.format("Receipt was valid but no transactions found with a known product id."));
					}
				}).asJsonObject();
		
		String transaction_identifier = transaction.getString("original_transaction_id");
		String expiresAt = transaction.getString("expires_date");
			
		return updateOrCreateSubscription(transaction_identifier, receipt, Instant.from(VerifyResponse.DATE_FORMATER.parse(expiresAt)), metadata);
	}
	
	public AppStoreTransaction verify(Receipt receipt)  throws ReceiptVerificationException, NoRecoredTransactionsException {
		return this.verify(receipt, new Hashtable<>());
	}

	public AppStoreTransaction verify(Receipt receipt, Map<Metadata, Boolean> metadata) throws ReceiptVerificationException, NoRecoredTransactionsException {
		
		String receiptData = receipt.getData();		
		
		return this.appStoreService.verify(receiptData, new VerifyResponse() {
			
			@Override
			public AppStoreTransaction verify(String bundle_id, JsonArray transactions)
					throws ReceiptVerificationException, NoRecoredTransactionsException {
				
				if (Bundle.of(bundle_id).isPresent()) {							
					return updateOrCreateSubscription(transactions, receiptData, metadata);
				} else {
					throw new ReceiptVerificationException(String.format("Receipt was valid but there was mismatch on the bundle id '%s'.", bundle_id));
				}
			}
		});
	}
}
