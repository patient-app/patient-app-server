package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import ch.uzh.ifi.imrg.patientapp.constant.ExerciseComponentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseComponentOutputDTO {
    private String id;
    private ExerciseComponentType exerciseComponentType;
    private String exerciseComponentDescription;
    private String userInput;
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private Integer orderNumber;
}
