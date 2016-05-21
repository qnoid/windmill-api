
package io.windmill.windmill.persistence;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries(
        @NamedQuery(name = "windmill.with_user_identifier",
                query = "SELECT w FROM Windmill w WHERE w.user.identifier = :user_identifier")
)
public class Windmill {
    @Id
    private Long id;

    @NotNull
    private String identifier;

    @NotNull
    private Double version;

    @NotNull
    private String title;

    @ManyToOne
    private User user;
    
    /**
     * 
     */
    public Windmill()
    {
      // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     */
    public Windmill(String identifier, Double version, String title)
    {
      this.identifier = identifier;
      this.version = version;
      this.title = title;
    }
}
