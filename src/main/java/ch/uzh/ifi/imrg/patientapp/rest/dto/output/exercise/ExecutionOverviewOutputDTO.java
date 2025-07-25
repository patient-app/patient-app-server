package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ExecutionOverviewOutputDTO {
    private Instant executionTitle;
    private String exerciseExecutionId;
}
