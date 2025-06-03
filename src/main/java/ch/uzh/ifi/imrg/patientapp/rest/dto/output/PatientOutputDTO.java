package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientOutputDTO {

    private String id;
    private String name;
    private String description;
    private String email;
    private String language;
    private boolean onboarded;

    public PatientOutputDTO() {
    }

}
