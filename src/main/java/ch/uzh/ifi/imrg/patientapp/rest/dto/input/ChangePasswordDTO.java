package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;

    @NotBlank
    private String confirmPassword;

}
