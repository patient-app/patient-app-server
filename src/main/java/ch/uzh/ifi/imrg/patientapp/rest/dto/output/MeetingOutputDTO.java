package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingOutputDTO {

    private String id;

    private String externalMeetingId;

    private String patientId;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String location;

}
