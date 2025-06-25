package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseImageElementOutputDTO extends ExerciseElementOutputDTO {
    private ImageData data;

    @Getter
    @Setter
    public static class ImageData {
        private String id;
        private String alt;
    }
}
