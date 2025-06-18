package ch.uzh.ifi.imrg.patientapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
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

    public Set<String> getAllTags(Patient patient) {

        Set<String> tags = journalEntryRepository.findDistinctTagsByPatientId(patient.getId());

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        tags = tags.stream().map(tag -> CryptographyUtil.decrypt(tag, key)).collect(Collectors.toSet());

        return tags;
    }

    public List<GetAllJournalEntriesDTO> listEntries(Patient patient) {

        List<JournalEntry> entries = journalEntryRepository.findByPatientId(patient.getId());

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        List<GetAllJournalEntriesDTO> result = new ArrayList<>();

        for (JournalEntry entry : entries) {

            GetAllJournalEntriesDTO dto = JournalEntryMapper.INSTANCE.convertEntitytoGetAllJournalEntriesDTO(entry);

            dto.setTitle(CryptographyUtil.decrypt(dto.getTitle(), key));
            dto.setTags(
                    dto.getTags().stream().map(tag -> CryptographyUtil.decrypt(tag, key)).collect(Collectors.toSet()));

            result.add(dto);
        }

        return result;
    }

    public JournalEntryOutputDTO getEntry(Patient patient, String entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "JournalEntry not found"));

        if (!entry.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found for that patient");
        }

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        JournalEntryOutputDTO dto = JournalEntryMapper.INSTANCE.convertEntityToJournalEntryOutputDTO(entry);

        decryptJournalDTO(dto, key);

        return dto;
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
