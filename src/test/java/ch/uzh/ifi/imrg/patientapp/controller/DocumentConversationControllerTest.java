package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Document.DocumentConversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DocumentConversationControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private MessageService messageService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private LogService logService;

    @InjectMocks
    private DocumentConversationController controller;

    @Test
    void testSendMessage_ReturnsMessageOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "docConv-001";

        CreateMessageDTO createDto = new CreateMessageDTO();
        createDto.setMessage("Hello Doc");

        Patient patient = new Patient();
        patient.setId("p123");

        Conversation conversation = new GeneralConversation();
        conversation.setId(conversationId); // Fix: mock the conversation

        Message answered = new Message();
        answered.setRequest("Hello Doc");
        answered.setResponse("Doc says hi");
        answered.setConversation(conversation); // Fix: set the conversation on the message

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(messageService.generateAnswer(patient, conversationId, "Hello Doc"))
                .thenReturn(answered);
        doNothing().when(logService).createLog(nullable(String.class), any(LogTypes.class), anyString());

        // Act
        MessageOutputDTO result = controller.sendMessage(request, createDto, conversationId);

        // Assert
        assertEquals("Hello Doc", result.getRequestMessage());
        assertEquals("Doc says hi", result.getResponseMessage());
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(messageService).generateAnswer(patient, conversationId, "Hello Doc");
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }

    @Test
    void testGetAllMessages_ReturnsDecryptedDocumentConversationOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "docConv-002";

        Patient patient = new Patient();
        patient.setId("p123");
        patient.setPrivateKey("encKey");

        // Create a DocumentConversation with one encrypted message
        DocumentConversation conv = new DocumentConversation();
        conv.setId(conversationId);

        Message m = new Message();
        m.setId("m100");
        m.setRequest("ENC_REQ");
        m.setResponse("ENC_RES");
        conv.getMessages().add(m);

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(conversationService.getAllMessagesFromConversation(conversationId, patient))
                .thenReturn(conv);

        // Static‐mock CryptographyUtil so that inner decrypt(privateKey) → plainKey
        try (var crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey"))
                    .thenReturn("plainKey");
            crypto.when(() -> CryptographyUtil.decrypt("ENC_REQ", "plainKey"))
                    .thenReturn("DecryptedReq");
            crypto.when(() -> CryptographyUtil.decrypt("ENC_RES", "plainKey"))
                    .thenReturn("DecryptedRes");

            // Act
            DocumentConversationOutputDTO dto = controller.getAllMessages(request, conversationId);

            // Assert top‐level fields
            assertEquals(conversationId, dto.getId());
            assertNotNull(dto.getMessages());
            assertEquals(1, dto.getMessages().size());

            // Assert the single message was decrypted and mapped
            MessageOutputDTO msgDto = dto.getMessages().get(0);
            assertEquals("DecryptedReq", msgDto.getRequestMessage());
            assertEquals("DecryptedRes", msgDto.getResponseMessage());
        }

        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).getAllMessagesFromConversation(conversationId, patient);
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }

    @Test
    void testDeleteDocumentChat_InvokesServiceCorrectly() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "docConv-003";

        Patient patient = new Patient();
        patient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        // Act
        controller.deleteDocumentChat(request, conversationId);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService)
                .deleteAllMessagesFromConversation(conversationId, patient);
        verifyNoMoreInteractions(patientService, messageService, conversationService);
    }
}
