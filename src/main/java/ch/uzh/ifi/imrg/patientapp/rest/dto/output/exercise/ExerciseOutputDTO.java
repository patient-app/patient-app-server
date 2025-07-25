package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ExerciseOutputDTO {
    private String exerciseExecutionId;
    private String exerciseTitle;
    private String exerciseDescription;
    private List<ExerciseComponentOutputDTO> exerciseComponents;
}
