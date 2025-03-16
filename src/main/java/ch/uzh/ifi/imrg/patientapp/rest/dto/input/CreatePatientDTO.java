package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

public class CreatePatientDTO {
    private String email;
    private String password;

    public CreatePatientDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
