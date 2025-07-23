package ch.uzh.ifi.imrg.patientapp.controller;

import java.util.List;
import java.util.Set;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalChatbotOutputDTO;

@RestController
public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final PatientService patientService;
    private final LogService logService;

    JournalEntryController(JournalEntryService journalEntryService, PatientService patientService, LogService logService) {
        this.journalEntryService = journalEntryService;
        this.patientService = patientService;
        this.logService = logService;
    }

    @PostMapping("/patients/journal-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryOutputDTO create(@Valid @RequestBody JournalEntryRequestDTO dto,
            HttpServletRequest httpServletRequest) {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        JournalEntryOutputDTO savedEntry = journalEntryService.createEntry(dto, loggedInPatient);

        logService.createLog(loggedInPatient.getId(), LogTypes.JOURNAL_CREATION,savedEntry.getId());
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
    @ResponseStatus(HttpStatus.OK)
    public JournalEntryOutputDTO getOne(@PathVariable String entryId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.getEntry(loggedInPatient, entryId);
    }

    @PutMapping("/patients/journal-entries/{entryId}")
    @ResponseStatus(HttpStatus.OK)
    public JournalEntryOutputDTO updateJournalEntry(@Valid @RequestBody JournalEntryRequestDTO dto,
            @PathVariable String entryId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.updateJournalEntry(loggedInPatient, entryId, dto);
    }

    @DeleteMapping("/patients/journal-entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable String entryId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        journalEntryService.deleteEntry(loggedInPatient, entryId);
    }

    @GetMapping("/patients/journal-entries/{entryId}/chatbot")
    @ResponseStatus(HttpStatus.OK)
    public JournalChatbotOutputDTO getAllMessages(@PathVariable String entryId,
            HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return journalEntryService.getJournalChatbot(loggedInPatient, entryId);
    }

}
