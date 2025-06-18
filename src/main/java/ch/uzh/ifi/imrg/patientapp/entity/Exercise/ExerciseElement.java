package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseTextElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // JOINED = clean normalized tables
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExerciseImageElement.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ExerciseFileElement.class, name = "FILE"),
        @JsonSubTypes.Type(value = ExerciseTextElement.class, name = "TEXT")
})
@Getter
@Setter
public abstract class ExerciseElement {

    @Id
    private String id;

    private String type;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    // Custom JSON output for 'data' block
    @JsonProperty("data")
    public abstract Object getData();
}
