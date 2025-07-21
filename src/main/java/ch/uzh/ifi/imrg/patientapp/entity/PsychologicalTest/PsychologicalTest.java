package ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "psychological_tests")
public class PsychologicalTest {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "completed_at", updatable = false)
    @CreationTimestamp
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @OneToMany(mappedBy = "psychologicalTest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PsychologicalTestQuestions> psychologicalTestsQuestions;

    @OneToMany(mappedBy = "psychologicalTest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PsychologicalTestCompletion> psychologicalTestCompletions;


    String name;
    String description;
    private int doEveryNDays;


}
