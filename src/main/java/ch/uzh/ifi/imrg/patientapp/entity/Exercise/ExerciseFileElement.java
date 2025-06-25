package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
public class ExerciseFileElement extends ExerciseElement {

    private String name;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stored_file_id", referencedColumnName = "id")
    private StoredExerciseFile fileId;

    public ExerciseFileElement() {
        this.setType("FILE");
    }


    @Override
    public Object getData() {
        return Map.of(
                "name", name,
                "fileId", fileId != null ? fileId.getId() : null
        );
    }

}

