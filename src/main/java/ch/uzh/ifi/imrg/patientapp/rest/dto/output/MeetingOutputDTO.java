package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.Instant;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingOutputDTO {

    private String id;

    private Instant createdAt;

    private Instant updatedAt;

    private String patientId;

    private Instant startAt;

    private Instant endAt;

    private String location;

    private MeetingStatus meetingStatus;

}
