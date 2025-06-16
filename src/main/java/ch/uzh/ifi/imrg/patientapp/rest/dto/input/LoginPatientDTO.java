package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginPatientDTO {
    private String email;
    private String username;
    @NotBlank
    private String password;

    public LoginPatientDTO() {
    }

}
