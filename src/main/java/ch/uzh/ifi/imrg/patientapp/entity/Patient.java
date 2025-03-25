package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String gender;

    @Column(nullable = true)
    private int age;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String description;

    @ManyToOne
    @JoinColumn(name = "therapist_id", referencedColumnName = "id")
    private Therapist therapist;

    @Column(unique = true)
    private String workspaceId = UUID.randomUUID().toString();

    @Column(name = "private_key", unique = true)
    private String privateKey;
    @OneToMany(mappedBy = "conversation_id", fetch = FetchType.LAZY)
    private List<Conversation> conversations;
}