package ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ExerciseUpdateInputDTO {
    private String exerciseTitle;
    private String exerciseDescription;
    private String exerciseExplanation;
    private Instant exerciseStart;
    private Instant exerciseEnd;
    private Boolean isPaused;
    private int doEveryNDays;
    private List<ExerciseComponentInputDTO> exerciseComponents;
}
