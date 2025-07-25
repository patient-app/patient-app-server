package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ExerciseCompletionNameInputDTO {
    private String exerciseExecutionId;
    private Instant executionTitle;
}
