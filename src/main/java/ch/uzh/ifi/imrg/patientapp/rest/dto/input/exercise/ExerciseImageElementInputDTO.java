package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseElementOutputDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseImageElementInputDTO extends ExerciseElementInputDTO {
    private ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseImageElementOutputDTO.ImageData data;

    @Getter
    @Setter
    public static class ImageData {
        private String data;
        private String alt;
    }
}
