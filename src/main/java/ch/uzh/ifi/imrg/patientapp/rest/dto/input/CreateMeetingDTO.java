package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMeetingDTO {

    private String externalMeetingId;

    // this is not needed, becaus patientId comes from path variable
    // private String patientId;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String location;

}
