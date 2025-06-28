package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PsychologicalTestInputDTO {

    private String name;

    private String description;

    // IDs of the questions to create with the test
    private List<PsychologicalTestQuestionInputDTO> questions;

    // Optionally, the patient id this test belongs to
    private String patientId;
}
