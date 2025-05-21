package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "chatbot_templates")
public class ChatbotTemplate implements Serializable {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String chatbotName;

    private String description;
    private String chatbotModel;
    private String chatbotIcon;
    private String chatbotLanguage;
    private String chatbotRole;
    private String chatbotTone;
    private String welcomeMessage;

    @Column(nullable = false)
    private String workspaceId;

    @ManyToOne
    @JoinColumn(name = "therapist_id", referencedColumnName = "id")
    private Therapist therapist;

}