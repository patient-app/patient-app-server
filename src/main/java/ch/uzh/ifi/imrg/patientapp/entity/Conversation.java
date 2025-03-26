package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Conversation implements Serializable {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "external_id", unique = true, nullable = false, updatable = false)
    private String externalId = UUID.randomUUID().toString();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_last")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "chatbotTemplate_id", fetch = FetchType.EAGER)
    private ChatbotTemplate chatbotTemplate;

    @Column(name = "conversation_summary", unique = true)
    private String conversationSummary;

    @Column(name = "conversation_context", unique = true)
    private String conversationContext;

    @Column(name = "conversation_instruction", unique = true)
    private String conversationInstruction;

    @OneToMany(mappedBy = "message_id", fetch = FetchType.LAZY)
    private List <Message> messages;
}
