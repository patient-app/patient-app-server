package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ExerciseInformation {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "created_at", updatable = false)
    private Instant startTime;
    private Instant endTime;
    private String feedback;
}
