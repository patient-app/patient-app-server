package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExerciseOutputDTO {
    private String id;
    private String title;
    private String description;
    private List<ExerciseElementDTO> elements;
}
