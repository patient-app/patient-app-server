package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseConversationControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private MessageService messageService;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ExerciseConversationController controller;

    @Test
    void testSendMessage_ReturnsMessageOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "conv-001";
        CreateMessageDTO createMessageDTO = new CreateMessageDTO();
        createMessageDTO.setMessage("Hello Exercise");

        Patient patient = new Patient();
        patient.setId("p123");

        Message message = new Message();
        message.setRequest("Hello Exercise");
        message.setResponse("Hi there!");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(messageService.generateAnswer(patient, conversationId, "Hello Exercise")).thenReturn(message);

        // Act
        MessageOutputDTO result = controller.sendMessage(request, createMessageDTO, conversationId);

        // Assert
        assertEquals("Hello Exercise", result.getRequestMessage());
        assertEquals("Hi there!", result.getResponseMessage());
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(messageService).generateAnswer(patient, conversationId, "Hello Exercise");
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }

    @Test
    void testGetAllMessages_ReturnsCompleteConversationOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "conv-002";

        Patient patient = new Patient();
        patient.setId("p123");
        patient.setPrivateKey("encKey");

        Message message = new Message();
        message.setRequest("encReq");
        message.setResponse("encRes");

        GeneralConversation conversation = new GeneralConversation();
        conversation.setId(conversationId);
        conversation.setMessages(List.of(message));

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.getAllMessagesFromConversation(conversationId, patient))
                .thenReturn(conversation);

        try (var cryptoMock = mockStatic(CryptographyUtil.class)) {
            cryptoMock.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("plainKey");
            cryptoMock.when(() -> CryptographyUtil.decrypt("encReq", "plainKey")).thenReturn("DecryptedReq");
            cryptoMock.when(() -> CryptographyUtil.decrypt("encRes", "plainKey")).thenReturn("DecryptedRes");

            // Act
            CompleteConversationOutputDTO result = controller.getAllMessages(request, conversationId);

            // Assert
            assertEquals(conversationId, result.getId());
            assertEquals(1, result.getMessages().size());
            MessageOutputDTO msgDTO = result.getMessages().getFirst();
            assertEquals("DecryptedReq", msgDTO.getRequestMessage());
            assertEquals("DecryptedRes", msgDTO.getResponseMessage());
        }

        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).getAllMessagesFromConversation(conversationId, patient);
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }

    @Test
    void testDeleteExerciseChat_InvokesServiceCorrectly() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "conv-003";

        Patient patient = new Patient();
        patient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        // Act
        controller.deleteExerciseChat(request, conversationId);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).deleteAllMessagesFromExerciseConversation(conversationId, patient);
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }
}
