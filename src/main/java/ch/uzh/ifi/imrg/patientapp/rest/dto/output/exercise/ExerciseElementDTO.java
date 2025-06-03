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
        @JsonSubTypes.Type(value = ExerciseImageElementDTO.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ExerciseFileElementDTO.class, name = "FILE"),
        @JsonSubTypes.Type(value = ExerciseTextInputElementDTO.class, name = "TEXT_INPUT")
})
public abstract class ExerciseElementDTO {
    private String id;
    private String type;
}
