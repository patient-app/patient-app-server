package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ExerciseInputDTO {
    private String name;
    private String description;
    private String pictureUrl;
    private List<ExerciseElementInputDTO> exerciseElements;
    @Override
    public String toString() {
        return "ExerciseInputDTO{" +
                "pictureUrl='" + pictureUrl + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", exerciseElements=" + (exerciseElements != null ? exerciseElements.toString() : "[]") +
                '}';
    }

}
