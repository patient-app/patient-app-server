package ch.uzh.ifi.imrg.patientapp.coachapi;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachGetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachJournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class CoachJournalEntryController {

    private final JournalEntryService journalEntryService;

    CoachJournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping("/coach/patients/{patientId}/journal-entries")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<CoachGetAllJournalEntriesDTO> listAll(@PathVariable String patientId) {
        return journalEntryService.getEntriesForCoach(patientId);
    }

    @GetMapping("/coach/patients/{patientId}/journal-entries/{entryId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public CoachJournalEntryOutputDTO getOne(@PathVariable String patientId, @PathVariable String entryId) {
        return journalEntryService.getOneEntryForCoach(patientId, entryId);
    }

}
