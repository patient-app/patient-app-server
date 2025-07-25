package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
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
        private Instant createdAt;

        @Column(nullable = true, columnDefinition = "TEXT")
        private String request;

        @Column(nullable = true, columnDefinition = "TEXT")
        private String response;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "conversation_id", nullable = false)
        private Conversation conversation;

        @Column(name = "in_system_prompt_summary", nullable = false)
        boolean inSystemPromptSummary = false;

        @Transient
        private String externalConversationId;

}
