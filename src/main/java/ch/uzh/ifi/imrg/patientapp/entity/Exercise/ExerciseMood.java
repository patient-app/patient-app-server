package ch.uzh.ifi.imrg.patientapp.entity.Exercise;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
public class ExerciseMood {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_mood_container_id", nullable = false)
    private ExerciseMoodContainer exerciseMoodContainer;

    private String moodName;
    private int moodScore;

}
