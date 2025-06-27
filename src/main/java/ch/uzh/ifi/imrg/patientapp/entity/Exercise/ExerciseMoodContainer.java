package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.List;


@Getter
@Setter
@Entity
public class ExerciseMoodContainer {
    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @OneToMany(mappedBy = "exerciseMoodContainer", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List <ExerciseMood> exerciseMoods;
}
