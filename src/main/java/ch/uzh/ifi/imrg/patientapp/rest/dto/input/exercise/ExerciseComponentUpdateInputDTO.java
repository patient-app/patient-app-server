package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import ch.uzh.ifi.imrg.patientapp.constant.ExerciseComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseComponentUpdateInputDTO {
    private String id;
    private ExerciseComponentType exerciseComponentType;
    private String exerciseComponentDescription;
    private String fileName;
    private String fileType;
    private String youtubeUrl;
    @Schema(type = "string", format = "byte", description = "File data encoded in Base64")
    private byte[] fileData;
    private Integer orderNumber;
}
