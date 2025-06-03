package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseTextInputElementDTO extends ExerciseElementDTO {
    private TextInputData data;

    @Getter
    @Setter
    public static class TextInputData {
        private String label;
        private String placeholder;
        private boolean required;
    }
}
