package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
        @Mock
        private MessageRepository messageRepository;

        @Mock
        private ConversationRepository conversationRepository;

        @Mock
        private PromptBuilderService promptBuilderService;

        @Mock
        private AuthorizationService authorizationService;

        @InjectMocks
        private MessageService messageService;

        @Test
        void generateAnswer_shouldEncryptMessageAndResponse_andSetFieldsCorrectly() {
                // Arrange
                String externalId = "conv-001";
                String inputMessage = "How are you?";
                String mockKey = "mock-decrypted-key";
                String encryptedMessage = "encrypted-message";
                String mockResponse = "<think>\n"
                                + "...some reasoning...\n"
                                + "</think> I’m fine, thank you!";
                String encryptedResponse = "encrypted-response";

                Patient patient = new Patient();
                patient.setPrivateKey("encrypted-pk");
                patient.setId("123");

                GeneralConversation conversation = new GeneralConversation();
                conversation.setId(externalId);
                conversation.setMessages(new ArrayList<>());
                conversation.setPatient(patient);

                ChatbotTemplate chatbotTemplate = new ChatbotTemplate();
                chatbotTemplate.setId("template-001");

                when(conversationRepository.findById(externalId))
                                .thenReturn(Optional.of(conversation));

                when(promptBuilderService.getResponse(
                                any(List.class),
                                eq(inputMessage),
                                nullable(String.class))).thenReturn(mockResponse);

                doNothing().when(authorizationService).checkConversationAccess(
                        eq(conversation),
                        eq(patient),
                        anyString()
                );

                when(promptBuilderService.getHarmRating(anyString()))
                        .thenReturn("harm-raw");

                when(promptBuilderService.extractContentFromResponse("harm-raw"))
                        .thenReturn("false");


                // Return the actual message passed into save(), not a dummy one
                when(messageRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)) {
                        // Mock static CryptographyUtil methods
                        mocked.when(() -> CryptographyUtil.decrypt("encrypted-pk")).thenReturn(mockKey);
                        mocked.when(() -> CryptographyUtil.encrypt(inputMessage, mockKey)).thenReturn(encryptedMessage);
                        mocked.when(() -> CryptographyUtil.encrypt(mockResponse, mockKey))
                                        .thenReturn(encryptedResponse);

                        // Act
                        Message result = messageService.generateAnswer(patient, externalId, inputMessage);

                        // Assert
                        assertNotNull(result);
                        assertEquals(inputMessage, result.getRequest());
                        assertEquals(externalId, result.getExternalConversationId());

                        verify(messageRepository).save(any(Message.class));
                        verify(messageRepository).flush();
                        verify(conversationRepository, times(2)).findById(externalId);
                }
        }

        @Test
        void generateAnswer_shouldThrowIfConversationNotFoundInitially() {
                // Arrange
                String externalId = "not-found-id";
                Patient patient = new Patient();
                when(conversationRepository.findById(externalId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(IllegalArgumentException.class,
                                () -> messageService.generateAnswer(patient, externalId, "message"));
        }

        @Test
        void generateAnswer_shouldThrowIfConversationNullCheckFails() {
                // Arrange
                String externalId = "conv-null";
                Patient patient = new Patient();
                when(conversationRepository.findById(externalId))
                                .thenReturn(Optional.ofNullable(null));

                // Act & Assert
                assertThrows(IllegalArgumentException.class,
                                () -> messageService.generateAnswer(patient, externalId, "message"));
        }

        @Test
        void generateAnswer_shouldNotOverrideCreatedAt_whenAlreadySet() {
                // Arrange
                String externalId = "conv-001";
                String inputMessage = "How are you?";
                String mockKey = "mock-decrypted-key";
                String encryptedMessage = "encrypted-message";
                String mockResponse = "<think>\n" +
                                "...some reasoning...\n" +
                                "</think> I’m fine, thanks!";
                String encryptedResponse = "encrypted-response";

                Patient patient = new Patient();
                patient.setPrivateKey("encrypted-pk");
                patient.setId("123");

                GeneralConversation conversation = new GeneralConversation();
                conversation.setId(externalId);
                conversation.setMessages(new ArrayList<>());
                conversation.setPatient(patient);

                ChatbotTemplate chatbotTemplate = new ChatbotTemplate();
                chatbotTemplate.setId("template-001");

                when(conversationRepository.findById(externalId))
                                .thenReturn(Optional.of(conversation));

                when(promptBuilderService.getResponse(
                                any(List.class),
                                eq(inputMessage),
                                nullable(String.class))).thenReturn(mockResponse);
                when(promptBuilderService.getHarmRating(anyString()))
                        .thenReturn("harm-raw");

                when(promptBuilderService.extractContentFromResponse("harm-raw"))
                        .thenReturn("false");

                // The real object that is passed to save and returned
                Message newMessage = new Message();
                Instant presetTime = Instant.ofEpochSecond(1704103200);
                newMessage.setCreatedAt(presetTime);

                when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
                        Message arg = invocation.getArgument(0);
                        arg.setCreatedAt(presetTime); // simulate it being set before returned
                        return arg;
                });

                try (MockedStatic<CryptographyUtil> utilities = mockStatic(CryptographyUtil.class)) {
                        utilities.when(() -> CryptographyUtil.decrypt("encrypted-pk")).thenReturn(mockKey);
                        utilities.when(() -> CryptographyUtil.encrypt(inputMessage, mockKey))
                                        .thenReturn(encryptedMessage);
                        utilities.when(() -> CryptographyUtil.encrypt(mockResponse, mockKey))
                                        .thenReturn(encryptedResponse);

                        // Act
                        Message result = messageService.generateAnswer(patient, externalId, inputMessage);

                        // Assert
                        assertNotNull(result.getCreatedAt());
                        assertEquals(presetTime, result.getCreatedAt(), "CreatedAt should not have been changed");
                }
        }

        @Test
        void testParseMessagesFromConversation_staticDecryptionMocked() throws Exception {
                // Arrange
                String key = "whateverKey";
                Message msg = new Message();
                msg.setRequest("encryptedRequest123");
                msg.setResponse("encryptedResponse456");

                GeneralConversation conversation = new GeneralConversation();
                conversation.setMessages(List.of(msg));

                try (MockedStatic<CryptographyUtil> mocked = org.mockito.Mockito.mockStatic(CryptographyUtil.class)) {
                        mocked.when(() -> CryptographyUtil.decrypt("encryptedRequest123", key))
                                        .thenReturn("Hello");
                        mocked.when(() -> CryptographyUtil.decrypt("encryptedResponse456", key))
                                        .thenReturn("Hi there!");

                        // Access private static method
                        Method method = MessageService.class.getDeclaredMethod("parseMessagesFromConversation",
                                        Conversation.class,
                                        String.class);
                        method.setAccessible(true);

                        // Act
                        List<Map<String, String>> result = (List<Map<String, String>>) method.invoke(null, conversation,
                                        key);

                        // Assert
                        assertEquals(2, result.size());
                        assertEquals("user", result.get(0).get("role"));
                        assertEquals("Hello", result.get(0).get("content"));
                        assertEquals("assistant", result.get(1).get("role"));
                        assertEquals("Hi there!", result.get(1).get("content"));
                }
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        void generateAnswer_shouldSummarizeOldMessagesAndMarkMessagesAsSummarized() {
                // Arrange
                String externalId = "conv-001";
                String inputMessage = "User question?";
                String mockKey = "mock-decrypted-key";
                String encryptedMessage = "encrypted-message";
                String mockSummary = "<summary>Summary content</summary>";
                String extractedSummary = "Summary content";
                String mockResponse = "<think>Reasoning</think> Answer here.";
                String encryptedResponse = "encrypted-response";

                Patient patient = new Patient();
                patient.setPrivateKey("encrypted-pk");
                patient.setId("123");

                GeneralConversation conversation = new GeneralConversation();
                conversation.setId(externalId);
                conversation.setMessages(new ArrayList<>());
                conversation.setPatient(patient);

                // Create >100 prior messages
                List<Message> priorMessagesEntities = new ArrayList<>();
                for (int i = 0; i < 110; i++) {
                        Message m = new Message();
                        m.setRequest("req-" + i);
                        m.setResponse("res-" + i);
                        priorMessagesEntities.add(m);
                }
                conversation.setMessages(priorMessagesEntities);

                // Simulated decrypted messages
                List<Map<String, String>> decryptedMessages = new ArrayList<>();
                for (int i = 0; i < 110; i++) {
                        decryptedMessages.add(Map.of("role", "user", "content", "message " + i));
                }

                when(conversationRepository.findById(externalId)).thenReturn(Optional.of(conversation));
                when(promptBuilderService.getResponse(
                        any(List.class),
                        eq(inputMessage),
                        nullable(String.class)
                )).thenReturn(mockResponse);
                when(promptBuilderService.getSummary(anyList(), nullable(String.class))).thenReturn(mockSummary);
                when(promptBuilderService.extractContentFromResponse(mockSummary)).thenReturn(extractedSummary);
                when(promptBuilderService.extractContentFromResponse(mockResponse)).thenReturn("Answer here.");
                when(promptBuilderService.getHarmRating(anyString()))
                        .thenReturn("harm-raw");

                when(promptBuilderService.extractContentFromResponse("harm-raw"))
                        .thenReturn("false");

                doNothing().when(authorizationService).checkConversationAccess(any(), any(), anyString());

                // Return the actual message
                when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // Provide conversation messages to be marked summarized
                List<Message> messagesToMark = new ArrayList<>();
                for (int i = 0; i < 30; i++) {
                        Message m = mock(Message.class);
                        messagesToMark.add(m);
                }
                when(messageRepository.findByConversationIdAndInSystemPromptSummaryFalseOrderByCreatedAt(externalId))
                        .thenReturn(messagesToMark);

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class);
                        MockedStatic<MessageService> parseMock = mockStatic(MessageService.class)) {

                        mocked.when(() -> CryptographyUtil.decrypt("encrypted-pk")).thenReturn(mockKey);
                        mocked.when(() -> CryptographyUtil.encrypt(inputMessage, mockKey)).thenReturn(encryptedMessage);
                        mocked.when(() -> CryptographyUtil.encrypt("Answer here.", mockKey)).thenReturn(encryptedResponse);

                        parseMock.when(() -> MessageService.parseMessagesFromConversation(any(Conversation.class), any(String.class)))
                                .thenReturn(decryptedMessages);

                        // Act
                        Message result = messageService.generateAnswer(patient, externalId, inputMessage);

                        // Assert
                        assertNotNull(result);
                        assertEquals(inputMessage, result.getRequest());
                        assertEquals("Answer here.", result.getResponse());
                        assertEquals(externalId, result.getExternalConversationId());

                        // Verify the summary was created and set
                        verify(promptBuilderService).getSummary(anyList(), nullable(String.class));
                        assertEquals(extractedSummary, conversation.getChatSummary());

                        // Verify that messages were marked summarized
                        for (int i = 0; i < 20; i++) {
                                verify(messagesToMark.get(i)).setInSystemPromptSummary(true);
                        }
                        // Only the first 20 should be marked, the last 10 should be untouched
                        for (int i = 20; i < 30; i++) {
                                verify(messagesToMark.get(i), never()).setInSystemPromptSummary(true);
                        }

                        verify(messageRepository, atLeastOnce()).flush();
                        verify(messageRepository).save(any(Message.class));
                        verify(conversationRepository, times(2)).findById(externalId);
                }

        }




}
