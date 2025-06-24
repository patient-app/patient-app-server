package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$", message = "Password must contain upper, lower and digits")
    private String newPassword;
}
