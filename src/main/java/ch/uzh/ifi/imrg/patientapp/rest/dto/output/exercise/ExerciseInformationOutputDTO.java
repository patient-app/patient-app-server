package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ExerciseInformationOutputDTO {
    private Instant startTime;
    private Instant endTime;
    private String feedback;
}
