package ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class PsychologicalTestCompletion {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "created_at", updatable = false)
    private Instant startTime;
    private Instant endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psychological_test_id", nullable = false)
    private PsychologicalTest psychologicalTest;

    @OneToMany(mappedBy = "psychologicalTestCompletionInformation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PsychologicalTestQuestionAnswer> questionAnswers;

}
