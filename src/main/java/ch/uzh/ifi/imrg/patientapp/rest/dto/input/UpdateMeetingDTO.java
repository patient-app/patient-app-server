package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;

@Getter
@Setter
public class UpdateMeetingDTO {

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String location;

    private MeetingStatus meetingStatus;
}
