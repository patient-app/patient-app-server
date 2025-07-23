package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutConversationNameDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.*;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
    private ChatbotService chatbotService;

    @InjectMocks
    private ConversationController conversationController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private LogService logService;


    @Test
    void createConversation_shouldReturnOutputDTO() {
        Patient patient = new Patient();
        GeneralConversation conversation = new GeneralConversation();
        conversation.setId("12345");


        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.createConversation(patient)).thenReturn(conversation);

        CreateConversationOutputDTO result = conversationController.createConversation(request);

        assertEquals("12345", result.getId());
        verify(patientService).addConversationToPatient(patient, conversation);
    }

    @Test
    void sendMessage_shouldReturnMessageOutputDTO() throws AccessDeniedException {
        Patient patient = new Patient();
        CreateMessageDTO dto = new CreateMessageDTO();
        dto.setMessage("Hello");

        Conversation conversation = new GeneralConversation();
        conversation.setId("cid123");

        Message message = new Message();
        message.setRequest("Hello");
        message.setResponse("Hi!");
        message.setConversation(conversation); // <-- Fix: set conversation

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(messageService.generateAnswer(patient, "cid123", "Hello")).thenReturn(message);
        doNothing().when(logService).createLog(nullable(String.class), any(LogTypes.class), anyString(), "");

        MessageOutputDTO result = conversationController.sendMessage(request, dto, "cid123");

        assertEquals("Hello", result.getRequestMessage());
        assertEquals("Hi!", result.getResponseMessage());
    }


    @Test
    void getAllMessages_shouldReturnDecryptedMessages() {
        Patient patient = new Patient();
        patient.setPrivateKey("encKey");

        GeneralConversation conversation = new GeneralConversation();
        conversation.setId("cid123");

        Message message = new Message();
        message.setRequest("encReq");
        message.setResponse("encRes");
        conversation.setMessages(Collections.singletonList(message));

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.getAllMessagesFromConversation("cid123", patient)).thenReturn(conversation);

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

    @Test
    void getConversationDTO_shouldReturnMappedConversationList() {
        // Arrange
        Patient mockPatient = new Patient();

        GeneralConversation conversation = new GeneralConversation();
        conversation.setId("cid123");
        conversation.setConversationName("Test Conversation");

        List<GeneralConversation> conversationList = Collections.singletonList(conversation);

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(conversationService.getAllConversationsFromPatient(mockPatient)).thenReturn(conversationList);

        // Act
        List<NameConversationOutputDTO> result = conversationController.getConversationNames(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("cid123", result.getFirst().getId());
        assertEquals("Test Conversation", result.getFirst().getName());
    }

    @Test
    void testUpdateSharing_CallsServicesCorrectly() {
        // Arrange
        String conversationId = "conv123";
        HttpServletRequest request = mock(HttpServletRequest.class);
        PutSharingDTO dto = new PutSharingDTO();
        dto.setShareWithAi(Boolean.TRUE);
        dto.setShareWithCoach(Boolean.TRUE);

        Patient mockPatient = new Patient();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String gender = "Male";
        mockPatient.setCreatedAt(createdAt);
        mockPatient.setUpdatedAt(updatedAt);
        mockPatient.setGender(gender);
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        conversationController.updateSharing(dto, conversationId, request);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).updateSharing(dto, conversationId, mockPatient);
        assertEquals(createdAt, mockPatient.getCreatedAt(), "createdAt should not have changed");
        assertEquals(updatedAt, mockPatient.getUpdatedAt(), "updatedAt should not have changed");
        assertEquals(gender, mockPatient.getGender(), "gender should not have changed");
    }

    @Test
    void testDeleteChat_CallsServicesCorrectly() {
        // Arrange
        String conversationId = "conv456";
        HttpServletRequest request = mock(HttpServletRequest.class);

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        conversationController.deleteChat(request, conversationId);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).deleteConversation(conversationId, mockPatient);
    }

    @Test
    void testPostConversationName_CallsServicesCorrectly() {
        // Arrange
        String conversationId = "conv789";
        PutConversationNameDTO dto = new PutConversationNameDTO();
        dto.setConversationName("New Name");

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        conversationController.postConversationName(dto, conversationId, request);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).setConversationName(dto, conversationId, mockPatient);
    }



}
