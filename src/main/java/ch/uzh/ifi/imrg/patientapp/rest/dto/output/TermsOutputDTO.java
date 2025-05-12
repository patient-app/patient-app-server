package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsOutputDTO {
    private String terms;

    public TermsOutputDTO(String terms) {
        this.terms = terms;
    }
}

