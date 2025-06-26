package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseMediaOutputDTO {
    private String id;
    private byte[] data;
}
