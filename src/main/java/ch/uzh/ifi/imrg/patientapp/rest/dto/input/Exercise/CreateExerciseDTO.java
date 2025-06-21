package ch.uzh.ifi.imrg.patientapp.rest.dto.input.Exercise;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseElementDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CreateExerciseDTO {
    private String name;
    private String description;
    private String pictureUrl;
    private List<ExerciseElementDTO> elements;
}
