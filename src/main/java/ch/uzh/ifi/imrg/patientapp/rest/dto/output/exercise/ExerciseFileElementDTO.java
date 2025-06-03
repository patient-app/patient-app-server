package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseFileElementDTO extends ExerciseElementDTO {
    private FileData data;
    @Getter
    @Setter
    public static class FileData {
        private String name;
        private String url;
    }
}
