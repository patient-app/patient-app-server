package ch.uzh.ifi.imrg.patientapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.JournalEntryMapper;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        JournalEntryOutputDTO savedEntry = journalEntryService.createEntry(dto, loggedInPatient);
        return savedEntry;
    }

    // get all entries for patient

    // get specific entry

    // update specific entry
}
