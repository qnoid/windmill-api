
package io.windmill.windmill.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.jboss.logging.Logger;

@Entity
@NamedQueries({
    @NamedQuery(name = "account.list", query = "SELECT a FROM Account a"),
    @NamedQuery(name = "account.find_by_identifier", query = "SELECT a FROM Account a WHERE a.identifier = :identifier")})
public class Account {
	
    private static final Logger LOGGER = Logger.getLogger(Account.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String identifier;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy="account")
    private Set<Windmill> windmills;

    /**
     * 
     */
    public Account()
    {
      // TODO Auto-generated constructor stub
    }
        
	public Account(String identifier) {
		this.identifier = identifier;
		this.windmills = new HashSet<Windmill>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void add(Windmill windmill) {
		windmill.account = this;		
		this.windmills.add(windmill);
		
        LOGGER.info(String.format("windmills: %s", windmills));
	}    
}
