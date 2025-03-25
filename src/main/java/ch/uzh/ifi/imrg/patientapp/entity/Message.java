package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Message implements Serializable {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = true)
    private String request;

    @Column(nullable = true)
    private String response;

    @Column(name = "message_context", nullable = true)
    private String messageContext;
}
