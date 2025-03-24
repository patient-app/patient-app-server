package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Conversation implements Serializable {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "access_token", unique = true, nullable = false, updatable = false)
    private String accessToken;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_last")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "chatbotTemplate_id", fetch = FetchType.EAGER)
    private ChatbotTemplate chatbotTemplate;

    @Column(unique = true)
    private String conversationSummary;

    @Column(unique = true)
    private String conversationContext;

    @Column(unique = true)
    private String conversationInstruction;
}
