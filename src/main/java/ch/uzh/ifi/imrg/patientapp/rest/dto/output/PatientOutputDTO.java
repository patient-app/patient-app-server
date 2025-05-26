package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientOutputDTO {

    private String id;
    private String name;
    private String createdAt;
    private String updatedAt;
    private String phoneNumber;
    private String address;
    private String gender;
    private String description;
    private int age;
    private String email;
    private String language;
    private boolean onboarded;

    public PatientOutputDTO() {
    }

    public PatientOutputDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
