package ch.uzh.ifi.imrg.patientapp.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.JournalEntryMapper;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public JournalEntryOutputDTO createEntry(JournalEntryRequestDTO dto, Patient loggedInPatient) {

        JournalEntry newEntry = JournalEntryMapper.INSTANCE.convertJournalEntryRequestDTOToEntity(dto);

        String key = CryptographyUtil.decrypt(loggedInPatient.getPrivateKey());

        encryptJournalEntity(newEntry, key);

        newEntry.setPatient(loggedInPatient);

        JournalEntry savedEntry = journalEntryRepository.saveAndFlush(newEntry);

        JournalEntryOutputDTO outputDTO = JournalEntryMapper.INSTANCE.convertEntityToJournalEntryOutputDTO(savedEntry);

        decryptJournalDTO(outputDTO, key);

        return outputDTO;
    }

    private void encryptJournalEntity(JournalEntry entry, String rawKey) {
        entry.setTitle(CryptographyUtil.encrypt(entry.getTitle(), rawKey));
        entry.setContent(CryptographyUtil.encrypt(entry.getContent(), rawKey));
        entry.setTags(
                entry.getTags().stream().map(tag -> CryptographyUtil.encrypt(tag, rawKey)).collect(Collectors.toSet()));
    }

    private void decryptJournalDTO(JournalEntryOutputDTO dto, String rawKey) {
        dto.setTitle(CryptographyUtil.decrypt(dto.getTitle(), rawKey));
        dto.setContent(CryptographyUtil.decrypt(dto.getContent(), rawKey));
        dto.setTags(
                dto.getTags().stream().map(tag -> CryptographyUtil.decrypt(tag, rawKey)).collect(Collectors.toSet()));
    }

}
