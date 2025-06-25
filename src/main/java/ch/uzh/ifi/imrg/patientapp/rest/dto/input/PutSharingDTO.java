package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutSharingDTO {
    private Boolean shareWithCoach;
    private Boolean shareWithAi;
}
