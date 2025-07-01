package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PsychologicalTestInputDTO {

    private String name;

    private String description;

    private List<PsychologicalTestQuestionInputDTO> questions;

    private String patientId;
}
