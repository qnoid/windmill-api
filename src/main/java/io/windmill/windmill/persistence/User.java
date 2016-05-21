
package io.windmill.windmill.persistence;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@NamedQueries({
    @NamedQuery(name = "user.list", query = "SELECT u FROM User u"),
    @NamedQuery(name = "user.find_by_identifier", query = "SELECT u FROM User u WHERE u.identifier = :identifier")})
public class User {
    @Id
    private Long id;

    @NotNull
    private String identifier;

    @OneToMany
    public Set<Windmill> windmills;

    /**
     * 
     */
    public User()
    {
      // TODO Auto-generated constructor stub
    }

}
