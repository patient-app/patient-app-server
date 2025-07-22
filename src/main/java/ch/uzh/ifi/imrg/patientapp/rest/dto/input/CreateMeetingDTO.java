package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMeetingDTO {

    private String id;

    // this is not needed, becaus patientId comes from path variable
    // private String patientId;

    private Instant startAt;

    private Instant endAt;

    private String location;

}
