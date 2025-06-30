package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ChatbotMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ChatbotService {
    private final PatientRepository patientRepository;
    private final ChatbotTemplateRepository chatbotTemplateRepository;

    public ChatbotService(PatientRepository patientRepository, ChatbotTemplateRepository chatbotTemplateRepository) {
        this.patientRepository = patientRepository;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
    }


    public void createChatbot(String patientId, CreateChatbotDTO createChatbotDTO) {
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("No patient found with this ID");
        }

        List<ChatbotTemplate> existing = chatbotTemplateRepository.findByPatientId(patient.getId());
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Chatbot template already exists for this patient: ");
        }

        ChatbotTemplate chatbotTemplate = ChatbotMapper.INSTANCE.convertCreateChatbotDTOToChatbotTemplate(createChatbotDTO);
        chatbotTemplate.setPatient(patient);
        chatbotTemplateRepository.save(chatbotTemplate);
    }


    public void updateChatbot(String patientId, UpdateChatbotDTO updateChatbotDTO) {
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("No patient found with ID: " + patientId);
        }
        ChatbotTemplate chatbotTemplate = chatbotTemplateRepository.findById(updateChatbotDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("No chatbot configuration found for patient ID: " + patientId));
        ChatbotMapper.INSTANCE.updateChatbotTemplateFromUpdateChatbotDTO(updateChatbotDTO, chatbotTemplate);
        chatbotTemplateRepository.save(chatbotTemplate);

    }

    public List<ChatbotConfigurationOutputDTO> getChatbotConfigurations(String patientId) {
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("No patient found with ID: " + patientId);
        }
        List<ChatbotTemplate> chatbotTemplates = chatbotTemplateRepository.findByPatientId(patient.getId());
        return ChatbotMapper.INSTANCE.chatbotTemplatesToChatbotConfigurationOutputDTOs(chatbotTemplates);
    }
}
