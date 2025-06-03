package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseImageElementDTO extends ExerciseElementDTO {
    private ImageData data;

    @Getter
    @Setter
    public static class ImageData {
        private String url;
        private String alt;
    }
}
