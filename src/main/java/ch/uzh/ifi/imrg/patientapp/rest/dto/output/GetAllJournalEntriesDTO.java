package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllJournalEntriesDTO {

    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;
    private Set<String> tags;
    private boolean sharedWithTherapist;
    private boolean aiAccessAllowed;

}
