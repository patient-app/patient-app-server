package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import java.time.Instant;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMeetingDTO {

    private String id;

    private Instant startAt;

    private Instant endAt;

    private MeetingStatus meetingStatus;

    private String location;

}
