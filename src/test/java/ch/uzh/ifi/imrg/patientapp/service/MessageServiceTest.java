package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.GetConversationSummaryInputDTO;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
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

        @Mock
        private LogService logService;

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
                                anyString());

                when(promptBuilderService.getHarmRating(anyString()))
                                .thenReturn("harm");

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
                                .thenReturn("harm");

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
                String mockSummary = "Summary content";
                String extractedSummary = "Summary content";
                String mockResponse = "Answer here.";
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
                                nullable(String.class))).thenReturn(mockResponse);
                when(promptBuilderService.getSummary(anyList(), nullable(String.class))).thenReturn(mockSummary);
                when(promptBuilderService.getHarmRating(anyString()))
                                .thenReturn("harm");

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
                        mocked.when(() -> CryptographyUtil.encrypt("Answer here.", mockKey))
                                        .thenReturn(encryptedResponse);

                        parseMock.when(() -> MessageService.parseMessagesFromConversation(any(Conversation.class),
                                        any(String.class)))
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

        @Test
        void getConversationSummary_shouldReturnNoMessagesWhenEmpty() {
                // Arrange
                GeneralConversation conversation = new GeneralConversation();
                conversation.setId("conv-001");
                GetConversationSummaryInputDTO dto = new GetConversationSummaryInputDTO();
                dto.setStart(Instant.now().minusSeconds(60));
                dto.setEnd(Instant.now());

                when(messageRepository.findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                eq("conv-001"),
                                any(),
                                any())).thenReturn(List.of());

                // Act
                String result = messageService.getConversationSummary(conversation, dto);

                // Assert
                assertEquals("No messages found in the specified time range.", result);
                verify(messageRepository).findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                eq("conv-001"),
                                any(),
                                any());
                verifyNoInteractions(promptBuilderService);
        }

        @Test
        void getConversationSummary_shouldDecryptParseAndSummarize() {
                // Arrange
                GeneralConversation conversation = new GeneralConversation();
                conversation.setId("conv-001");
                Patient patient = new Patient();
                patient.setPrivateKey("encrypted-key");
                conversation.setPatient(patient);

                GetConversationSummaryInputDTO dto = new GetConversationSummaryInputDTO();
                dto.setStart(Instant.now().minusSeconds(60));
                dto.setEnd(Instant.now());

                Message msg = new Message();
                msg.setRequest("enc-req");
                msg.setResponse("enc-resp");

                List<Message> messagesList = List.of(msg);

                when(messageRepository.findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                any(), any(), any())).thenReturn(messagesList);

                List<Map<String, String>> parsedMessages = List.of(
                                Map.of("role", "user", "content", "Hello"),
                                Map.of("role", "assistant", "content", "Hi"));

                try (MockedStatic<CryptographyUtil> cryptographyUtilMocked = mockStatic(CryptographyUtil.class);
                                MockedStatic<MessageService> parseMessagesMocked = mockStatic(MessageService.class)) {

                        cryptographyUtilMocked.when(() -> CryptographyUtil.decrypt("encrypted-key"))
                                        .thenReturn("decrypted-key");

                        parseMessagesMocked
                                        .when(() -> MessageService.parseMessages(eq(messagesList), eq("decrypted-key")))
                                        .thenReturn(parsedMessages);

                        when(promptBuilderService.getSummary(eq(parsedMessages), eq(""))).thenReturn("Summary result");

                        // Act
                        String result = messageService.getConversationSummary(conversation, dto);

                        // Assert
                        assertEquals("Summary result", result);
                        verify(messageRepository).findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                        any(), any(), any());
                        verify(promptBuilderService).getSummary(eq(parsedMessages), eq(""));
                }
        }

        @Test
        void getConversationSummary_shouldThrowWhenDecryptFails() {
                // Arrange
                GeneralConversation conversation = new GeneralConversation();
                conversation.setId("conv-001");
                Patient patient = new Patient();
                patient.setPrivateKey("bad-key");
                conversation.setPatient(patient);

                GetConversationSummaryInputDTO dto = new GetConversationSummaryInputDTO();
                dto.setStart(Instant.now().minusSeconds(60));
                dto.setEnd(Instant.now());

                Message msg = new Message();
                msg.setRequest("enc-req");
                msg.setResponse("enc-resp");

                when(messageRepository.findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                any(), any(), any())).thenReturn(List.of(msg));

                try (MockedStatic<CryptographyUtil> cryptographyUtilMocked = mockStatic(CryptographyUtil.class)) {
                        cryptographyUtilMocked.when(() -> CryptographyUtil.decrypt("bad-key"))
                                        .thenThrow(new RuntimeException("Decryption failed"));

                        // Act & Assert
                        RuntimeException ex = assertThrows(
                                        RuntimeException.class,
                                        () -> messageService.getConversationSummary(conversation, dto));
                        assertTrue(ex.getMessage().contains("Decryption failed"));
                }
        }

        @Test
        void getConversationSummary_shouldThrowWhenPromptBuilderFails() {
                // Arrange
                GeneralConversation conversation = new GeneralConversation();
                conversation.setId("conv-001");
                Patient patient = new Patient();
                patient.setPrivateKey("some-key");
                conversation.setPatient(patient);

                GetConversationSummaryInputDTO dto = new GetConversationSummaryInputDTO();
                dto.setStart(Instant.now().minusSeconds(60));
                dto.setEnd(Instant.now());

                Message msg = new Message();
                msg.setRequest("enc-req");
                msg.setResponse("enc-resp");

                List<Message> messagesList = List.of(msg);

                when(messageRepository.findByConversationIdAndCreatedAtBetweenOrderByCreatedAt(
                                any(), any(), any())).thenReturn(messagesList);

                List<Map<String, String>> parsedMessages = List.of(
                                Map.of("role", "user", "content", "Hi"));

                try (MockedStatic<CryptographyUtil> cryptographyUtilMocked = mockStatic(CryptographyUtil.class);
                                MockedStatic<MessageService> parseMessagesMocked = mockStatic(MessageService.class)) {

                        cryptographyUtilMocked.when(() -> CryptographyUtil.decrypt("some-key"))
                                        .thenReturn("decrypted-key");

                        parseMessagesMocked
                                        .when(() -> MessageService.parseMessages(eq(messagesList), eq("decrypted-key")))
                                        .thenReturn(parsedMessages);

                        when(promptBuilderService.getSummary(eq(parsedMessages), eq("")))
                                        .thenThrow(new RuntimeException("Summarization error"));

                        // Act & Assert
                        RuntimeException ex = assertThrows(
                                        RuntimeException.class,
                                        () -> messageService.getConversationSummary(conversation, dto));
                        assertTrue(ex.getMessage().contains("Summarization error"));
                }
        }

        @Test
        void parseMessages_shouldReturnEmptyListWhenNoMessages() {
                List<Map<String, String>> result = MessageService.parseMessages(List.of(), "key");
                assertNotNull(result);
                assertTrue(result.isEmpty());
        }

        @Test
        void parseMessages_shouldSkipMessagesWithEmptyFields() {
                Message m1 = new Message();
                m1.setRequest(null);
                m1.setResponse("   ");

                Message m2 = new Message();
                m2.setRequest(" ");
                m2.setResponse(null);

                List<Message> input = List.of(m1, m2);

                List<Map<String, String>> result = MessageService.parseMessages(input, "key");

                assertNotNull(result);
                assertTrue(result.isEmpty());
        }

        @Test
        void parseMessages_shouldDecryptRequestOnly() {
                Message msg = new Message();
                msg.setRequest("encrypted-req");
                msg.setResponse(null);

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)) {
                        mocked.when(() -> CryptographyUtil.decrypt("encrypted-req", "key"))
                                        .thenReturn("decrypted-request");

                        List<Map<String, String>> result = MessageService.parseMessages(List.of(msg), "key");

                        assertEquals(1, result.size());
                        assertEquals("user", result.get(0).get("role"));
                        assertEquals("decrypted-request", result.get(0).get("content"));
                }
        }

        @Test
        void parseMessages_shouldDecryptResponseOnly() {
                Message msg = new Message();
                msg.setRequest(null);
                msg.setResponse("encrypted-resp");

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)) {
                        mocked.when(() -> CryptographyUtil.decrypt("encrypted-resp", "key"))
                                        .thenReturn("decrypted-response");

                        List<Map<String, String>> result = MessageService.parseMessages(List.of(msg), "key");

                        assertEquals(1, result.size());
                        assertEquals("assistant", result.get(0).get("role"));
                        assertEquals("decrypted-response", result.get(0).get("content"));
                }
        }

        @Test
        void parseMessages_shouldDecryptBothRequestAndResponse() {
                Message msg = new Message();
                msg.setRequest("enc-req");
                msg.setResponse("enc-resp");

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)) {
                        mocked.when(() -> CryptographyUtil.decrypt("enc-req", "key")).thenReturn("REQ");
                        mocked.when(() -> CryptographyUtil.decrypt("enc-resp", "key")).thenReturn("RESP");

                        List<Map<String, String>> result = MessageService.parseMessages(List.of(msg), "key");

                        assertEquals(2, result.size());

                        assertEquals("user", result.get(0).get("role"));
                        assertEquals("REQ", result.get(0).get("content"));

                        assertEquals("assistant", result.get(1).get("role"));
                        assertEquals("RESP", result.get(1).get("content"));
                }
        }

        @Test
        void parseMessages_shouldHandleMultipleMessages() {
                Message m1 = new Message();
                m1.setRequest("req1");
                m1.setResponse(null);

                Message m2 = new Message();
                m2.setRequest(null);
                m2.setResponse("resp2");

                try (MockedStatic<CryptographyUtil> mocked = mockStatic(CryptographyUtil.class)) {
                        mocked.when(() -> CryptographyUtil.decrypt("req1", "k")).thenReturn("R1");
                        mocked.when(() -> CryptographyUtil.decrypt("resp2", "k")).thenReturn("A2");

                        List<Map<String, String>> result = MessageService.parseMessages(List.of(m1, m2), "k");

                        assertEquals(2, result.size());

                        assertEquals("user", result.get(0).get("role"));
                        assertEquals("R1", result.get(0).get("content"));

                        assertEquals("assistant", result.get(1).get("role"));
                        assertEquals("A2", result.get(1).get("content"));
                }
        }

        @Test
        void generateAnswer_shouldLogWhenHarmfulContentDetected() {
                // Arrange
                String conversationId = "conv-123";
                String inputMessage = "I want to hurt myself.";
                String decryptedKey = "decrypted-key";
                String encryptedMessage = "encrypted-message";
                String encryptedResponse = "encrypted-response";
                String mockAnswer = "Please talk to someone you trust.";

                Patient patient = new Patient();
                patient.setPrivateKey("encrypted-pk");

                GeneralConversation conversation = new GeneralConversation();
                conversation.setId(conversationId);
                conversation.setMessages(new ArrayList<>());
                conversation.setPatient(patient);
                conversation.setSystemPrompt("system-prompt");

                when(conversationRepository.findById(conversationId))
                                .thenReturn(Optional.of(conversation));
                doNothing().when(authorizationService).checkConversationAccess(any(), any(), anyString());

                try (MockedStatic<CryptographyUtil> cryptographyUtilMock = mockStatic(CryptographyUtil.class);
                                MockedStatic<MessageService> parseMessagesMock = mockStatic(MessageService.class)) {

                        // Mock decrypting patient key
                        cryptographyUtilMock.when(() -> CryptographyUtil.decrypt("encrypted-pk"))
                                        .thenReturn(decryptedKey);

                        // Mock encrypting request and response
                        cryptographyUtilMock.when(() -> CryptographyUtil.encrypt(inputMessage, decryptedKey))
                                        .thenReturn(encryptedMessage);
                        cryptographyUtilMock.when(() -> CryptographyUtil.encrypt(mockAnswer, decryptedKey))
                                        .thenReturn(encryptedResponse);

                        // Mock parsing prior messages to return an empty list
                        parseMessagesMock.when(
                                        () -> MessageService.parseMessagesFromConversation(any(), eq(decryptedKey)))
                                        .thenReturn(List.of());

                        // Make harm rating "true" to hit the branch
                        when(promptBuilderService.getHarmRating(inputMessage)).thenReturn("true");

                        // Mock getting response
                        when(promptBuilderService.getResponse(anyList(), eq(inputMessage), eq("system-prompt")))
                                        .thenReturn(mockAnswer);
                        doNothing().when(logService).createLog(nullable(String.class), eq(LogTypes.HARMFUL_CONTENT_DETECTED), eq(conversationId), eq("Potentially harmful message: \"I want to hurt myself.\""));
                        // Act
                        Message result = messageService.generateAnswer(patient, conversationId, inputMessage);

                        // Assert
                        assertNotNull(result);
                        assertEquals(inputMessage, result.getRequest());
                        assertEquals(mockAnswer, result.getResponse());
                        assertEquals(conversationId, result.getExternalConversationId());

                        // Basic verifications
                        verify(promptBuilderService).getHarmRating(inputMessage);
                        verify(promptBuilderService).getResponse(anyList(), eq(inputMessage), eq("system-prompt"));
                        verify(messageRepository).save(any(Message.class));
                        verify(messageRepository).flush();
                        verify(conversationRepository, atLeastOnce()).findById(conversationId);
                }
        }

}
