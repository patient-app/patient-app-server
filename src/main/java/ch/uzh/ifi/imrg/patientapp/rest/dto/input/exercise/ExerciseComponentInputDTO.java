package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import ch.uzh.ifi.imrg.patientapp.constant.ExerciseComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseComponentInputDTO {
    private String id;
    private ExerciseComponentType exerciseComponentType;
    private String exerciseComponentDescription;
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private Integer orderNumber;
}
