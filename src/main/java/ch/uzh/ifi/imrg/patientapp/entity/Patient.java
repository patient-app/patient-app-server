package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    private String id = UUID.randomUUID().toString();

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

    @Getter
    @Column(nullable = false)
    private boolean admin = false;

    @ManyToOne
    @JoinColumn(name = "therapist_id", referencedColumnName = "id")
    private Therapist therapist;

    @Column(unique = true)
    private String workspaceId = UUID.randomUUID().toString();

    @Column(name = "private_key", unique = true)
    private String privateKey;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<Conversation> conversations;

    @Override
    public String toString() {
        return "Patient{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                ", admin=" + admin +
                ", therapistId=" + (therapist != null ? therapist.getId() : "null") +
                ", workspaceId='" + workspaceId + '\'' +
                ",language:"+ language + '\'' +
                ",onboarded:"+ onboarded + '\'' +
                ", privateKey=" + (privateKey != null ? privateKey : "null") +
                ", conversationCount=" + (conversations != null ? conversations.size() : 0) +
                '}';
    }

}