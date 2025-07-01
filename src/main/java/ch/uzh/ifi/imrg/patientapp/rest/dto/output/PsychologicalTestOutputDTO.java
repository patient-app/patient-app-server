package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PsychologicalTestOutputDTO {

    private String name;

    private String description;

    private List<PsychologicalTestQuestionOutputDTO> questions;

    private String patientId;

    public PsychologicalTestOutputDTO(String name) {
        this.name = name;
    }
}