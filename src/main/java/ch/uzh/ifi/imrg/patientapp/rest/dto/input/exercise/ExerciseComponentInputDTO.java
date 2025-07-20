package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import ch.uzh.ifi.imrg.patientapp.constant.ExerciseComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "string", format = "byte", description = "File data encoded in Base64")
    private byte[] fileData;
    private Integer orderNumber;
}
