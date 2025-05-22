package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UpdateMeetingDTO {

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String location;

}
