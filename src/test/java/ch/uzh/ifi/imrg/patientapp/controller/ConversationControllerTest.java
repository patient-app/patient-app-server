package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationControllerTest {
    @Mock
    private PatientService patientService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ConversationController conversationController;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        conversationController = new ConversationController(patientService, conversationService, messageService);
    }

    @Test
    void createConversation_shouldReturnOutputDTO() {
        Patient patient = new Patient();
        Conversation conversation = new Conversation();
        conversation.setExternalId("12345");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.createConversation(patient)).thenReturn(conversation);

        CreateConversationOutputDTO result = conversationController.createConversation(request);

        assertEquals("12345", result.getId());
        verify(patientService).addConversationToPatient(patient, conversation);
    }

    @Test
    void sendMessage_shouldReturnMessageOutputDTO() {
        Patient patient = new Patient();
        CreateMessageDTO dto = new CreateMessageDTO();
        dto.setMessage("Hello");

        Message message = new Message();
        message.setRequest("Hello");
        message.setResponse("Hi!");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(messageService.generateAnswer(patient, "cid123", "Hello")).thenReturn(message);

        MessageOutputDTO result = conversationController.sendMessage(request, dto, "cid123");
        assertEquals("Hello", result.getRequestMessage());
        assertEquals("Hi!", result.getResponseMessage());
    }

    @Test
    void getAllMessages_shouldReturnDecryptedMessages() {
        Patient patient = new Patient();
        patient.setPrivateKey("encKey");

        Conversation conversation = new Conversation();
        conversation.setExternalId("cid123");

        Message message = new Message();
        message.setRequest("encReq");
        message.setResponse("encRes");
        conversation.setMessages(Collections.singletonList(message));

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.getAllMessagesFromConversation("cid123")).thenReturn(conversation);

        try (var cryptoMock = mockStatic(CryptographyUtil.class)) {
            cryptoMock.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("plainKey");
            cryptoMock.when(() -> CryptographyUtil.decrypt("encReq", "plainKey")).thenReturn("req");
            cryptoMock.when(() -> CryptographyUtil.decrypt("encRes", "plainKey")).thenReturn("res");

            CompleteConversationOutputDTO result = conversationController.getAllMessages(request, "cid123");
            assertEquals("cid123", result.getId());
            assertEquals(1, result.getMessages().size());
            MessageOutputDTO resultMessage = result.getMessages().getFirst();
            assertEquals("req", resultMessage.getRequestMessage());
            assertEquals("res", resultMessage.getResponseMessage());
        }
    }

}
