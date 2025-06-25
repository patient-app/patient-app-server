package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
public class ExerciseTextElement extends ExerciseElement {

    private String label;
    private String placeholder;
    private boolean required;

    public ExerciseTextElement() {
        this.setType("TEXT_INPUT");
    }

    @Override
    public Object getData() {
        return Map.of(
                "label", label,
                "placeholder", placeholder,
                "required", required
        );
    }
}

