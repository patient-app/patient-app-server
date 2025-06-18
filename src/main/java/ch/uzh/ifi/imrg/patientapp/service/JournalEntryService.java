package ch.uzh.ifi.imrg.patientapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.JournalEntryMapper;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public JournalEntry createEntry(JournalEntryRequestDTO dto, Patient loggedInPatient) {
        JournalEntry newEntry = JournalEntryMapper.INSTANCE.convertJournalEntryRequestDTOToEntity(dto);
        newEntry.setPatient(loggedInPatient);
        return journalEntryRepository.save(newEntry);
    }

}
