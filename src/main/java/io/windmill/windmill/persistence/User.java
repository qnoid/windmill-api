
package io.windmill.windmill.persistence;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@NamedQueries(@NamedQuery(name = "user.list", query = "SELECT u FROM User u"))
public class User {
    @Id
    private Long id;

    @NotNull
    private String identifier;

    @OneToMany
    public Set<Windmill> windmills;



}
