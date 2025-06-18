package ch.uzh.ifi.imrg.patientapp.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;

@RestController
public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final PatientService patientService;

    JournalEntryController(JournalEntryService journalEntryService, PatientService patientService) {
        this.journalEntryService = journalEntryService;
        this.patientService = patientService;
    }

    @PostMapping("/patients/journal-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryOutputDTO create(@Valid @RequestBody JournalEntryRequestDTO dto,
            HttpServletRequest httpServletRequest) {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        JournalEntryOutputDTO savedEntry = journalEntryService.createEntry(dto, loggedInPatient);
        return savedEntry;
    }

    @GetMapping("/patients/journal-entries/tags")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> getAllTags(HttpServletRequest httpServletRequest) {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.getAllTags(loggedInPatient);
    }

    @GetMapping("/patients/journal-entries")
    @ResponseStatus(HttpStatus.OK)
    public List<GetAllJournalEntriesDTO> listAll(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.listEntries(loggedInPatient);
    }

    @GetMapping("/patients/journal-entries/{entryId}")
    public JournalEntryOutputDTO getOne(@PathVariable String entryId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.getEntry(loggedInPatient, entryId);
    }

    // TODO: update specific entry
}
