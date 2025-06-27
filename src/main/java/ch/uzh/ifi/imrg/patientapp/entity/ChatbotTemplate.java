package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    private String chatbotIcon;
    private String chatbotLanguage;
    private String chatbotRole;
    private String chatbotTone;
    private String welcomeMessage;


}