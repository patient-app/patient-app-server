package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PsychologicalTestOutputDTO {

    private String name;

    private String description;

    private String patientId;

    private Instant completedAt;

    private List<PsychologicalTestQuestionOutputDTO> questions;


    public PsychologicalTestOutputDTO(String name) {
        this.name = name;
    }
}