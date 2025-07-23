package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;


@Getter
@Setter
public class LogOutputDTO {
    private String id;
    private String patientId;
    private LogTypes logType;
    private Instant timestamp;
    private String uniqueIdentifier;
}
