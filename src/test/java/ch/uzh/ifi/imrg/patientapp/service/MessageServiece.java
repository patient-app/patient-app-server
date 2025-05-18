package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageServiece {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private PromptBuilderService promptBuilderService;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateAnswer_shouldEncryptMessageAndResponse_andSetFieldsCorrectly() {
        // Arrange
        String externalId = "conv-001";
        String inputMessage = "How are you?";
        String mockKey = "mock-decrypted-key";
        String encryptedMessage = "encrypted-message";
        String mockResponse = "I’m fine, thank you!";
        String encryptedResponse = "encrypted-response";

        Patient patient = new Patient();
        patient.setPrivateKey("encrypted-pk");

        Conversation conversation = new Conversation();
        conversation.setExternalId(externalId);

        Message savedMessage = new Message();
        savedMessage.setCreatedAt(LocalDateTime.now());

        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));
        when(promptBuilderService.getResponse(false,"")).thenReturn(mockResponse);
        when(messageRepository.save(any())).thenReturn(savedMessage);
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));

        try (
                MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)
        ) {
            mocked.when(() -> CryptographyUtil.decrypt("encrypted-pk")).thenReturn(mockKey);
            mocked.when(() -> CryptographyUtil.encrypt(inputMessage, mockKey)).thenReturn(encryptedMessage);
            mocked.when(() -> CryptographyUtil.encrypt(mockResponse, mockKey)).thenReturn(encryptedResponse);

            // Act
            Message result = messageService.generateAnswer(patient, externalId, inputMessage);

            // Assert
            assertNotNull(result);
            assertEquals(inputMessage, result.getRequest());
            assertEquals(mockResponse, result.getResponse());
            assertEquals(externalId, result.getExternalConversationId());

            verify(messageRepository).save(any(Message.class));
            verify(messageRepository).flush();
            verify(conversationRepository, times(2)).getConversationByExternalId(externalId);
        }
    }


    @Test
    void generateAnswer_shouldThrowIfConversationNotFoundInitially() {
        // Arrange
        String externalId = "not-found-id";
        Patient patient = new Patient();
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                messageService.generateAnswer(patient, externalId, "message")
        );
    }

    @Test
    void generateAnswer_shouldThrowIfConversationNullCheckFails() {
        // Arrange
        String externalId = "conv-null";
        Patient patient = new Patient();
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.ofNullable(null));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                messageService.generateAnswer(patient, externalId, "message")
        );
    }

    @Test
    void generateAnswer_shouldNotOverrideCreatedAt_whenAlreadySet() {
        // Arrange
        String externalId = "conv-001";
        String inputMessage = "How are you?";
        String mockKey = "mock-decrypted-key";
        String encryptedMessage = "encrypted-message";
        String mockResponse = "I’m fine, thanks!";
        String encryptedResponse = "encrypted-response";

        Patient patient = new Patient();
        patient.setPrivateKey("encrypted-pk");

        Conversation conversation = new Conversation();
        conversation.setExternalId(externalId);

        try (MockedStatic<CryptographyUtil> utilities = mockStatic(CryptographyUtil.class)) {
            when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));
            when(promptBuilderService.getResponse(false,"")).thenReturn(mockResponse);
            when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));

            utilities.when(() -> CryptographyUtil.decrypt("encrypted-pk")).thenReturn(mockKey);
            utilities.when(() -> CryptographyUtil.encrypt(inputMessage, mockKey)).thenReturn(encryptedMessage);
            utilities.when(() -> CryptographyUtil.encrypt(mockResponse, mockKey)).thenReturn(encryptedResponse);

            // The real object that is passed to save and returned
            Message newMessage = new Message();
            LocalDateTime presetTime = LocalDateTime.of(2024, 1, 1, 10, 0);
            newMessage.setCreatedAt(presetTime);

            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
                Message arg = invocation.getArgument(0);
                arg.setCreatedAt(presetTime); // simulate it being set before returned
                return arg;
            });

            // Act
            Message result = messageService.generateAnswer(patient, externalId, inputMessage);

            // Assert
            assertNotNull(result.getCreatedAt());
            assertEquals(presetTime, result.getCreatedAt(), "CreatedAt should not have been changed");
        }
    }



}
