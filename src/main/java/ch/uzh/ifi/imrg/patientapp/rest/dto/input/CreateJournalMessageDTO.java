package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJournalMessageDTO {
    private String message;
    private String journalTitle;
    private String journalContent;
}
