package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginPatientDTO {
    private String email;
    private String password;

    public LoginPatientDTO() {
    }

}
