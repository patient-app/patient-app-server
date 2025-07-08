package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ExerciseInputDTO {
    private String name;
    private String exerciseDescription;
    private String exerciseExplanation;
    private String description;
    private String pictureData;
    private List<ExerciseElementInputDTO> exerciseElements;
}
