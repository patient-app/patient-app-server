package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class PsychologicalTestAssignmentOutputDTO {

    private String patientId;
    private String testName;
    private Instant exerciseStart;
    private Instant exerciseEnd;
    private Boolean isPaused;
    private int doEveryNDays;

}
