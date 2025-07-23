package ch.uzh.ifi.imrg.patientapp.entity;

import ch.uzh.ifi.imrg.patientapp.constant.ChatBotAvatar;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient implements Serializable {

    @Id
    @Column(unique = true)
    private String id;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String gender;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String language;

    @Column(nullable = true)
    private boolean onboarded;

    @Column(name = "private_key", unique = true)
    private String privateKey;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GeneralConversation> conversations;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Exercise> exercises;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Meeting> meetings;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<JournalEntry> journalEntries;

    @OneToOne(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ChatbotTemplate chatbotTemplate;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PsychologicalTest> psychologicalTests;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDocument> patientDocuments = new ArrayList<>();

    @Column(name = "coach_access_key", nullable = false)
    private String coachAccessKey;

    @Column(name = "coach_email", nullable = true)
    private String coachEmail;

    @Column(name = "chat_bot_avatar", nullable = true)
    private ChatBotAvatar chatBotAvatar = ChatBotAvatar.NONE;

}