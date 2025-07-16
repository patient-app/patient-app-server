package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.GetConversationSummaryInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ConversationSummaryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatbotServiceTest {
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;
    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageService messageService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private PromptBuilderService promptBuilderService;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    void testConstructor_AssignsRepositories() {
        // Arrange
        PatientRepository patientRepository = Mockito.mock(PatientRepository.class);
        ChatbotTemplateRepository chatbotTemplateRepository = Mockito.mock(ChatbotTemplateRepository.class);
        ConversationRepository conversationRepository = Mockito.mock(ConversationRepository.class);
        MessageService messageService = Mockito.mock(MessageService.class);
        PromptBuilderService promptBuilderService = Mockito.mock(PromptBuilderService.class);
        AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);

        // Act
        ChatbotService chatbotService = new ChatbotService(patientRepository, chatbotTemplateRepository,
                conversationRepository, messageService, promptBuilderService, authorizationService);

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

    @Test
    void testCreateChatbot_ThrowsWhenTemplateAlreadyExists() {
        // Arrange
        String patientId = "patient123";
        CreateChatbotDTO createChatbotDTO = new CreateChatbotDTO();

        Patient patient = new Patient();
        patient.setId(patientId);

        ChatbotTemplate existingTemplate = new ChatbotTemplate();

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId)).thenReturn(List.of(existingTemplate));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> chatbotService.createChatbot(patientId, createChatbotDTO)
        );

        assertTrue(ex.getMessage().contains("Chatbot template already exists"));

        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }

    @Test
    void testGetWelcomeMessage_ThrowsWhenPatientNotFound() {
        // Arrange
        String patientId = "patient123";
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.getWelcomeMessage(patientId)
        );
        assertTrue(ex.getMessage().contains("No patient found with ID: " + patientId));

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(chatbotTemplateRepository);
    }

    @Test
    void testGetWelcomeMessage_ReturnsDefaultMessageWhenNoTemplates() {
        // Arrange
        String patientId = "patient123";
        Patient patient = new Patient();
        patient.setId(patientId);

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId)).thenReturn(List.of());

        // Act
        String result = chatbotService.getWelcomeMessage(patientId);

        // Assert
        assertEquals("Welcome to your chatbot! Please configure it first.", result);

        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }

    @Test
    void testGetWelcomeMessage_ReturnsTemplateMessageWhenTemplateExists() {
        // Arrange
        String patientId = "patient123";
        Patient patient = new Patient();
        patient.setId(patientId);

        ChatbotTemplate template = new ChatbotTemplate();
        template.setWelcomeMessage("Hello, welcome back!");

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId)).thenReturn(List.of(template));

        // Act
        String result = chatbotService.getWelcomeMessage(patientId);

        // Assert
        assertEquals("Hello, welcome back!", result);

        verify(patientRepository).getPatientById(patientId);
        verify(chatbotTemplateRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(patientRepository, chatbotTemplateRepository);
    }

    @Test
    void getConversationSummary_ShouldThrow_WhenPatientNotFound() {
        // Arrange
        String patientId = "patient123";
        GetConversationSummaryInputDTO inputDTO = new GetConversationSummaryInputDTO();
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.getConversationSummary(patientId, inputDTO)
        );
        assertTrue(ex.getMessage().contains("No patient found with ID: " + patientId));

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(conversationRepository, authorizationService, messageService, promptBuilderService);
    }

    @Test
    void getConversationSummary_ShouldThrow_WhenNoConversationsFound() {
        // Arrange
        String patientId = "patient123";
        GetConversationSummaryInputDTO inputDTO = new GetConversationSummaryInputDTO();
        Patient patient = new Patient();
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(conversationRepository.getConversationsSharedWithCoachByPatientId(patientId)).thenReturn(List.of());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatbotService.getConversationSummary(patientId, inputDTO)
        );
        assertTrue(ex.getMessage().contains("No conversations found"));

        verify(patientRepository).getPatientById(patientId);
        verify(conversationRepository).getConversationsSharedWithCoachByPatientId(patientId);
        verifyNoMoreInteractions(patientRepository, conversationRepository);
        verifyNoInteractions(authorizationService, messageService, promptBuilderService);
    }

    @Test
    void getConversationSummary_ShouldReturnSummary_WhenConversationsExist() {
        // Arrange
        String patientId = "patient123";
        GetConversationSummaryInputDTO inputDTO = new GetConversationSummaryInputDTO();
        Patient patient = new Patient();
        GeneralConversation conversation1 = new GeneralConversation();
        GeneralConversation conversation2 = new GeneralConversation();
        List<GeneralConversation> conversations = List.of(conversation1, conversation2);

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(conversationRepository.getConversationsSharedWithCoachByPatientId(patientId)).thenReturn(conversations);

        when(messageService.getConversationSummary(conversation1, inputDTO)).thenReturn("Summary1");
        when(messageService.getConversationSummary(conversation2, inputDTO)).thenReturn("Summary2");
        when(promptBuilderService.getSummaryOfAllConversations(List.of("Summary1", "Summary2"))).thenReturn("Overall summary");

        // Act
        ConversationSummaryOutputDTO result = chatbotService.getConversationSummary(patientId, inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Overall summary", result.getConversationSummary());

        verify(patientRepository).getPatientById(patientId);
        verify(conversationRepository).getConversationsSharedWithCoachByPatientId(patientId);
        verify(authorizationService).checkConversationAccess(conversation1, patient, "You do not have access to conversations, which are not yours.");
        verify(messageService).getConversationSummary(conversation1, inputDTO);
        verify(messageService).getConversationSummary(conversation2, inputDTO);
        verify(promptBuilderService).getSummaryOfAllConversations(List.of("Summary1", "Summary2"));
    }



}
