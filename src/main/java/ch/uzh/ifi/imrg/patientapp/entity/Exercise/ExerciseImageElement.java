package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
public class ExerciseImageElement extends ExerciseElement {

    private String url;
    private String alt;

    public ExerciseImageElement() {
        this.setType("IMAGE");
    }

    @Override
    public Object getData() {
        return Map.of("url", url, "alt", alt);
    }
}

