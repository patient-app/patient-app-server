package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ExerciseInformationInputDTO {
    private Instant startTime;
    private Instant endTime;
    private String feedback;
}
