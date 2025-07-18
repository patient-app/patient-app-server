package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseCompletionNameInputDTO {
    private String exerciseExecutionId;
    String executionTitle;
}
