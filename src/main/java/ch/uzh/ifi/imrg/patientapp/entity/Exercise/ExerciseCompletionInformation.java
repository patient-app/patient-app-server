package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ExerciseCompletionInformation {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "created_at", updatable = false)
    private Instant startTime;
    private Instant endTime;
    private String executionTitle;
    private String feedback;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ExerciseMoodContainer exerciseMoodBefore;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ExerciseMoodContainer exerciseMoodAfter;

    @OneToMany(mappedBy = "completionInformation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseComponentAnswer> componentAnswers = new ArrayList<>();

    public Optional<ExerciseComponentAnswer> getComponentAnswerById(String componentId) {
        if (componentId == null) return Optional.empty();
        return componentAnswers.stream()
                .filter(answer -> componentId.equals(answer.getExerciseComponentId()))
                .findFirst();
    }

}
