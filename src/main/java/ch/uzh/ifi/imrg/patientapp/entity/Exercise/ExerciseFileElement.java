package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
public class ExerciseFileElement extends ExerciseElement {

    private String name;
    private String url;

    public ExerciseFileElement() {
        this.setType("FILE");
    }

    @Override
    public Object getData() {
        return Map.of("name", name, "url", url);
    }
}

