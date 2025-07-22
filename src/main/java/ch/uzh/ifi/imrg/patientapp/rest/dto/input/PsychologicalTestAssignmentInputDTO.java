package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class PsychologicalTestAssignmentInputDTO {
    private String patientId;
    private String testName;
    private Instant exerciseStart;
    private Instant exerciseEnd;
    private Boolean isPaused;
    private int doEveryNDays;


}
