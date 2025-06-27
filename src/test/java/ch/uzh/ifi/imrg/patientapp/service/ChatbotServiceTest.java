package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ChatbotMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatbotServiceTest {
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    void testConstructor_AssignsRepositories() {
        // Arrange
        PatientRepository patientRepository = Mockito.mock(PatientRepository.class);
        ChatbotTemplateRepository chatbotTemplateRepository = Mockito.mock(ChatbotTemplateRepository.class);

        // Act
        ChatbotService chatbotService = new ChatbotService(patientRepository, chatbotTemplateRepository);

        // Assert
        assertNotNull(chatbotService);

    }

    @Test
    void testCreateChatbot_SavesChatbotWhenPatientExists() {
        // Arrange
        String patientId = "patient123";
        CreateChatbotDTO createChatbotDTO = new CreateChatbotDTO();
        Patient patient = new Patient();
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        // Act
        chatbotService.createChatbot(patientId, createChatbotDTO);

        // Assert
        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).save(Mockito.<ChatbotTemplate>any());
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }



    @Test
    void testCreateChatbot_ThrowsWhenPatientNotFound() {
        // Arrange
        String patientId = "patient123";
        CreateChatbotDTO createChatbotDTO = new CreateChatbotDTO();
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.createChatbot(patientId, createChatbotDTO)
        );
        assertTrue(ex.getMessage().contains("No patient found"));

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(chatbotTemplateRepository);
    }

    @Test
    void testUpdateChatbot_SuccessfullyUpdatesAndSaves() {
        // Arrange
        String patientId = "patient123";
        String templateId = "template456";
        UpdateChatbotDTO dto = new UpdateChatbotDTO();
        dto.setId(templateId);

        Patient patient = new Patient();
        ChatbotTemplate template = new ChatbotTemplate();

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // Act
        chatbotService.updateChatbot(patientId, dto);

        // Assert
        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findById(templateId);
        verify(chatbotTemplateRepository).save(template);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }

    @Test
    void testUpdateChatbot_ThrowsWhenPatientNotFound() {
        // Arrange
        String patientId = "patient123";
        UpdateChatbotDTO dto = new UpdateChatbotDTO();
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.updateChatbot(patientId, dto)
        );
        assertTrue(ex.getMessage().contains("No patient found"));

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(chatbotTemplateRepository);
    }

    @Test
    void testUpdateChatbot_ThrowsWhenTemplateNotFound() {
        // Arrange
        String patientId = "patient123";
        String templateId = "template456";
        UpdateChatbotDTO dto = new UpdateChatbotDTO();
        dto.setId(templateId);

        Patient patient = new Patient();
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.updateChatbot(patientId, dto)
        );
        assertTrue(ex.getMessage().contains("No chatbot configuration found"));

        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findById(templateId);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }

    @Test
    void testGetChatbotConfigurations_ReturnsMappedDTOs() {
        // Arrange
        String patientId = "patient123";
        Patient patient = new Patient();
        patient.setId(patientId);

        ChatbotTemplate template = new ChatbotTemplate();
        List<ChatbotTemplate> templates = List.of(template);

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId)).thenReturn(templates);

        // Act
        List<ChatbotConfigurationOutputDTO> result = chatbotService.getChatbotConfigurations(patientId);

        // Assert
        assertNotNull(result);
        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }


    @Test
    void testGetChatbotConfigurations_ThrowsWhenPatientNotFound() {
        // Arrange
        String patientId = "patient123";
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.getChatbotConfigurations(patientId)
        );
        assertTrue(ex.getMessage().contains("No patient found"));

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(chatbotTemplateRepository);
    }


}
