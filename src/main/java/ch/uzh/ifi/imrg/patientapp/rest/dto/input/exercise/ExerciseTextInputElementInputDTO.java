package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseTextInputElementInputDTO extends ExerciseElementInputDTO {
    private ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseTextInputElementOutputDTO.TextInputData data;

    @Getter
    @Setter
    public static class TextInputData {
        private String label;
        private String placeholder;
        private boolean required;
    }
}
