package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExerciseImageElementOutputDTO.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ExerciseFileElementOutputDTO.class, name = "FILE"),
        @JsonSubTypes.Type(value = ExerciseTextInputElementOutputDTO.class, name = "TEXT_INPUT")
})
public abstract class ExerciseElementOutputDTO {
    private String id;
    private String type;
}
