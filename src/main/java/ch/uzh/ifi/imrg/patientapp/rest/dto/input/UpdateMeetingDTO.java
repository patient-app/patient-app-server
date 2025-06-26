package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;

@Getter
@Setter
public class UpdateMeetingDTO {

    private Instant startAt;

    private Instant endAt;

    private String location;

    private MeetingStatus meetingStatus;
}
