package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
public class ExerciseComponentAnswer {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();
    private String exerciseComponentId;

    @Lob
    @Column()
    private String userInput;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_information_id", nullable = false)
    private ExerciseCompletionInformation completionInformation;
}
