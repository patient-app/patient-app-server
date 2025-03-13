package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatbotName() {
        return chatbotName;
    }

    public void setChatbotName(String chatbotName) {
        this.chatbotName = chatbotName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChatbotModel() {
        return chatbotModel;
    }

    public void setChatbotModel(String chatbotModel) {
        this.chatbotModel = chatbotModel;
    }

    public String getChatbotIcon() {
        return chatbotIcon;
    }

    public void setChatbotIcon(String chatbotIcon) {
        this.chatbotIcon = chatbotIcon;
    }

    public String getChatbotLanguage() {
        return chatbotLanguage;
    }

    public void setChatbotLanguage(String chatbotLanguage) {
        this.chatbotLanguage = chatbotLanguage;
    }

    public String getChatbotRole() {
        return chatbotRole;
    }

    public void setChatbotRole(String chatbotRole) {
        this.chatbotRole = chatbotRole;
    }

    public String getChatbotTone() {
        return chatbotTone;
    }

    public void setChatbotTone(String chatbotTone) {
        this.chatbotTone = chatbotTone;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Therapist getTherapist() {
        return therapist;
    }

    public void setTherapist(Therapist therapist) {
        this.therapist = therapist;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}