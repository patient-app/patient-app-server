package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message implements Serializable {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "external_id", unique = true, nullable = false, updatable = false)
    private String externalId = UUID.randomUUID().toString();

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String request;

    @Column(nullable = true)
    private String response;

    @Column(name = "message_context", nullable = true)
    private String messageContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Transient
    private String externalConversationId;
}
