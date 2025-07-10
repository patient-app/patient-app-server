package ch.uzh.ifi.imrg.patientapp.entity.Exercise;


import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Exercise implements Serializable {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    private String name;
    private String pictureId;
    private String description;
    private boolean hasFeedback;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseElement> exerciseElements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List <ExerciseInformation> exerciseInformation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exercise_conversation_id", referencedColumnName = "id")
    private ExerciseConversation exerciseConversation;


}
