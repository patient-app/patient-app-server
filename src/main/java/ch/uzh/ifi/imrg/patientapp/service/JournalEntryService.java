package ch.uzh.ifi.imrg.patientapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntryConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachGetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachJournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalChatbotOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.JournalEntryMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    private final PatientRepository patientRepository;

    private final ChatbotTemplateRepository chatbotTemplateRepository;

    private final PromptBuilderService promptBuilderService;
    private final LogService logService;

    public JournalEntryService(JournalEntryRepository journalEntryRepository, PatientRepository patientRepository,
                               ChatbotTemplateRepository chatbotTemplateRepository, PromptBuilderService promptBuilderService, LogService logService) {
        this.journalEntryRepository = journalEntryRepository;
        this.patientRepository = patientRepository;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
        this.promptBuilderService = promptBuilderService;
        this.logService = logService;
    }

    public JournalEntryOutputDTO createEntry(JournalEntryRequestDTO dto, Patient loggedInPatient) {

        JournalEntry newEntry = JournalEntryMapper.INSTANCE.convertJournalEntryRequestDTOToEntity(dto);

        String key = CryptographyUtil.decrypt(loggedInPatient.getPrivateKey());

        encryptJournalEntity(newEntry, key);

        newEntry.setPatient(loggedInPatient);

        // create journalChatbot
        ChatbotTemplate chatbotTemplate = chatbotTemplateRepository.findByPatientId(loggedInPatient.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No chatbot template found for patient with ID: " + loggedInPatient.getId()));

        JournalEntryConversation conversation = new JournalEntryConversation();
        conversation.setPatient(loggedInPatient);
        conversation.setSystemPrompt(
                promptBuilderService.getJournalSystemPrompt(chatbotTemplate, dto.getTitle(), dto.getContent()));
        conversation.setConversationName("journal-chatbot");

        newEntry.setJournalEntryConversation(conversation);

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

    public JournalEntryOutputDTO updateJournalEntry(Patient patient, String entryId,
            JournalEntryRequestDTO dto) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal Entry not found"));

        if (!entry.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found for this patient");
        }

        BeanUtils.copyProperties(dto, entry);

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        encryptJournalEntity(entry, key);

        JournalEntry updatedEntry = journalEntryRepository.saveAndFlush(entry);

        JournalEntryOutputDTO outputDTO = JournalEntryMapper.INSTANCE
                .convertEntityToJournalEntryOutputDTO(updatedEntry);

        decryptJournalDTO(outputDTO, key);

        logService.createLog(patient.getId(), LogTypes.JOURNAL_UPDATE,entryId, "");
        return outputDTO;

    }

    public void deleteEntry(Patient patient, String entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal Entry not found"));

        if (!entry.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found for this patient");
        }

        journalEntryRepository.delete(entry);
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

    private void decryptJournalDTO(CoachJournalEntryOutputDTO dto, String rawKey) {
        dto.setTitle(CryptographyUtil.decrypt(dto.getTitle(), rawKey));
        dto.setContent(CryptographyUtil.decrypt(dto.getContent(), rawKey));
        dto.setTags(
                dto.getTags().stream().map(tag -> CryptographyUtil.decrypt(tag, rawKey)).collect(Collectors.toSet()));
    }

    public List<CoachGetAllJournalEntriesDTO> getEntriesForCoach(String patientId) {
        List<JournalEntry> entries = journalEntryRepository.findAllByPatientIdAndSharedWithTherapistTrue(patientId);

        Patient patient = patientRepository.getPatientById(patientId);

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        List<CoachGetAllJournalEntriesDTO> result = new ArrayList<>();

        for (JournalEntry entry : entries) {

            CoachGetAllJournalEntriesDTO dto = JournalEntryMapper.INSTANCE
                    .convertEntityToCoachGetAllJournalEntriesDTO(entry);

            dto.setTitle(CryptographyUtil.decrypt(dto.getTitle(), key));
            dto.setTags(
                    dto.getTags().stream().map(tag -> CryptographyUtil.decrypt(tag, key)).collect(Collectors.toSet()));

            result.add(dto);
        }

        return result;
    }

    public CoachJournalEntryOutputDTO getOneEntryForCoach(String patientId, String entryId) {
        JournalEntry entry = journalEntryRepository.findByIdAndSharedWithTherapistTrue(entryId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Journal entry not found or not shared with therapist"));

        Patient patient = patientRepository.getPatientById(patientId);

        if (!entry.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found for that patient");
        }

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        CoachJournalEntryOutputDTO dto = JournalEntryMapper.INSTANCE.convertEntityToCoachJournalEntryOutputDTO(entry);

        decryptJournalDTO(dto, key);

        return dto;
    }

    public JournalChatbotOutputDTO getJournalChatbot(Patient patient, String entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal Entry not found"));

        if (!entry.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found for that patient");
        }

        JournalEntryConversation conversation = entry.getJournalEntryConversation();
        if (conversation == null) {
            throw new IllegalArgumentException("No conversation found for exercise with ID: " + entryId);
        }
        return JournalEntryMapper.INSTANCE.convertEntityToJournalChatbotOutputDTO(conversation);
    }

}
