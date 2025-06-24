package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseFileElementInputDTO extends ExerciseElementInputDTO {
    private ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseFileElementOutputDTO.FileData data;
    @Getter
    @Setter
    public static class FileData {
        private String name;
        private String url;
    }
}
