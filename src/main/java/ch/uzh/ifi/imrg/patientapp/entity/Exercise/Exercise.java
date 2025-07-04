package ch.uzh.ifi.imrg.patientapp.entity.Exercise;


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

    @Override
    public String toString() {
        StringBuilder elementsStr = new StringBuilder();
        if (exerciseElements != null) {
            for (ExerciseElement element : exerciseElements) {
                elementsStr.append("\n  - [")
                        .append(element.getClass().getSimpleName())
                        .append("] id=")
                        .append(element.getId())
                        .append(", type=")
                        .append(element.getType())
                        .append(", data=")
                        .append(element.getData());
            }
        }

        return "Exercise {" +
                "\n  id='" + id + '\'' +
                ",\n  name='" + name + '\'' +
                ",\n  description='" + description + '\'' +
                ",\n  pictureUrl='" + pictureId + '\'' +
                ",\n  patientId='" + (patient != null ? patient.getId() : "null") + '\'' +
                ",\n  exerciseElements=" + elementsStr +
                "\n}";
    }

}
