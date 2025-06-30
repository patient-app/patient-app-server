package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseMoodOutputDTO {
    private String moodName;
    private int moodScore;

    @Override
    public String toString() {
        return "ExerciseMoodInputDTO{" +
                "moodName='" + moodName + '\'' +
                ", moodScore=" + moodScore +
                '}';
    }
}