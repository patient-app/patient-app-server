package ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
public class PsychologicalTestQuestionAnswer {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    String question;
    int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psychological_test_completion_information_id", nullable = false)
    private PsychologicalTestCompletion psychologicalTestCompletion;
}
