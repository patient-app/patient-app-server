package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.Instant;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JournalEntryOutputDTO {

    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private String title;
    private String content;
    private Set<String> tags;
    private boolean sharedWithTherapist;
    private boolean aiAccessAllowed;

}
