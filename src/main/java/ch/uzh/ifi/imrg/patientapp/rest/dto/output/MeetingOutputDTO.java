package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingOutputDTO {

    private String id;

    private String externalMeetingId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String patientId;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String location;

    private MeetingStatus meetingStatus;

}
