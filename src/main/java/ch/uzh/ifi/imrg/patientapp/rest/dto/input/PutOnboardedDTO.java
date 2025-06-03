package ch.uzh.ifi.imrg.patientapp.rest.dto.input;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutOnboardedDTO {
    private boolean onboarded;
    public PutOnboardedDTO() {
    }
}
