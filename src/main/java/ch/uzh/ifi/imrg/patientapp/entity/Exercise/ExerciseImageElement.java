package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
public class ExerciseImageElement extends ExerciseElement {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stored_file_id", referencedColumnName = "id")
    private StoredExerciseFile fileId;
    private String alt;

    public ExerciseImageElement() {
        this.setType("IMAGE");
    }

    @Override
    public Object getData() {
        return Map.of(
                "alt", alt,
                "fileId", fileId != null ? fileId.getId() : null
        );
    }

}

