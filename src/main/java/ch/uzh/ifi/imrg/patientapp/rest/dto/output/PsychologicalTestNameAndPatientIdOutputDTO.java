package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PsychologicalTestNameAndPatientIdOutputDTO {
    private String name;
    private String patientId;
    public PsychologicalTestNameAndPatientIdOutputDTO(String name, String patientId) {
        this.name = name;
        this.patientId = patientId;
    }
}
