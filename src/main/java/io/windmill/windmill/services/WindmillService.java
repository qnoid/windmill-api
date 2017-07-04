package io.windmill.windmill.services;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.persistence.WindmillDAO;

@ApplicationScoped
public class WindmillService {

    private static final Logger LOGGER = Logger.getLogger(WindmillService.class.getName());

	@Inject
	private WindmillDAO windmillDAO;

	public List<Windmill> get(String account_identifier) {
		
	      List<Windmill> windmills = this.windmillDAO.windmills(account_identifier);
	      
	      LOGGER.debug(String.format("Found: %s", windmills.size()));
	      
	      return windmills;
	}
}
