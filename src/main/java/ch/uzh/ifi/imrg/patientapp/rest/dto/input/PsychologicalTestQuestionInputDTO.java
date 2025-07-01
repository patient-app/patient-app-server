package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PsychologicalTestQuestionInputDTO {

    private String question;

    private int score;
}