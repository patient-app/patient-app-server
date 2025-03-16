package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

public class LoginPatientDTO {
    private String email;
    private String password;

    public LoginPatientDTO() {
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
