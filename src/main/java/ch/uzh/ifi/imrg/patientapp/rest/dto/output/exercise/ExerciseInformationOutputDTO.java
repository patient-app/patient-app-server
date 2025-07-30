package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseMoodInputDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ExerciseInformationOutputDTO {
    private Instant startTime;
    private Instant endTime;
    private String feedback;
    private List<SharedInputFieldOutputDTO> sharedInputFields;
    private List<ExerciseMoodOutputDTO> moodsBefore;
    private List<ExerciseMoodOutputDTO> moodsAfter;
}
