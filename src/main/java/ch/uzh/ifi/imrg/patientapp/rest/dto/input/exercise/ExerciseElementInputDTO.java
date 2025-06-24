package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

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
        @JsonSubTypes.Type(value = ExerciseImageElementInputDTO.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ExerciseFileElementInputDTO.class, name = "FILE"),
        @JsonSubTypes.Type(value = ExerciseTextInputElementInputDTO.class, name = "TEXT_INPUT")
})
public abstract class ExerciseElementInputDTO {
    private String id;
    private String type;
}
