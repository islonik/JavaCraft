package my.javacraft.soap2rest.rest.app.dao;

import jakarta.persistence.*;

/**
 * Created by nikilipa on 3/28/17.
 */
@Entity
@Table(name = "s2r_messages")
@NamedQueries({
        @NamedQuery(name = "findById", query =
                "select messages from Message messages where messages.id = :id"
        ),
        @NamedQuery(name = "findAll", query =
                "select messages from Message messages"
        )
})
public class Message {

    @Id
    private Long id;

    @Column(name = "message")
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

