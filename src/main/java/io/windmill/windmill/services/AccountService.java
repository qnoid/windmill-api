package io.windmill.windmill.services;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.AccountDAO;
import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.persistence.WindmillDAO;

@ApplicationScoped
public class AccountService {

    private static final Logger LOGGER = Logger.getLogger(AccountService.class);

	@Inject
	private WindmillDAO windmillDAO;

    @Inject
    private AccountDAO accountDAO;

	public Windmill create(String account_identifier, String windmill_identifier, String windmill_title,
			Double windmill_version) {

		Windmill windmill = this.windmillDAO.findOrCreate(windmill_identifier, identifier -> new Windmill(windmill_identifier, windmill_version, windmill_title));
		windmill.setUpdatedAt(Instant.now());
		
		Account account = this.accountDAO.findOrCreate(account_identifier);
		
		LOGGER.debug(String.format("Found: %s", account.getIdentifier()));            
		
		account.add(windmill);
		
		this.accountDAO.saveOrUpdate(account);
		
		return windmill;
	}
}
