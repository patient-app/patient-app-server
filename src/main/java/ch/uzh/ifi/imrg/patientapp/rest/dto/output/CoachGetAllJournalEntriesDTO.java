package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import java.time.Instant;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachGetAllJournalEntriesDTO {

    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private String title;
    private Set<String> tags;

}
