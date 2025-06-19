package ch.uzh.ifi.imrg.patientapp.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @Column
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column
    private String content;

    @ElementCollection
    @CollectionTable(name = "journal_entry_tags", joinColumns = @JoinColumn(name = "journal_entry_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "shared_with_therapist", nullable = false)
    private boolean sharedWithTherapist = false;

    @Column(name = "ai_access_allowed", nullable = false)
    private boolean aiAccessAllowed = false;

}
