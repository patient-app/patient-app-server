package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ExerciseInformationInputDTO {
    private String exerciseExecutionId;
    private Instant startTime;
    private Instant endTime;
    private String feedback;
    private List<ExerciseMoodInputDTO> moodsBefore;
    private List<ExerciseMoodInputDTO> moodsAfter;

}
