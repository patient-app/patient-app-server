package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePatientDTO {
    private String email;
    private String password;

    public CreatePatientDTO() {
    }

}
