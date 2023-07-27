package my.javacraft.soap2rest.rest.app.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message")
    private String message;

}

