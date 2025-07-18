package ch.uzh.ifi.imrg.patientapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntryConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateJournalMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class JournalEntryConversationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PatientService patientService;
    @Mock
    private MessageService messageService;
    @Mock
    private ConversationService conversationService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JournalEntryConversationController controller;

    @BeforeEach
    void setup() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void sendMessage_returnsMessageOutput() throws Exception {
        // Arrange
        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);

        CreateJournalMessageDTO reqDto = new CreateJournalMessageDTO();
        reqDto.setJournalEntryTitle("entryTitle");
        reqDto.setJournalEntryContent("entryContent");
        reqDto.setMessage("Hello");

        doNothing().when(conversationService)
                .updateJournalConversationSystemPrompt(mockPatient, "conv1",
                        reqDto.getJournalEntryTitle(), reqDto.getJournalEntryContent());

        Message msg = new Message();
        msg.setId("m1");
        Instant ts = Instant.parse("2025-06-26T10:15:30Z");
        msg.setCreatedAt(ts);
        msg.setRequest("Hello");
        msg.setResponse("Hi there!");
        JournalEntryConversation conv = new JournalEntryConversation();
        conv.setId("conv1");
        msg.setConversation(conv);
        msg.setExternalConversationId("conv1"); // ← add this line

        when(messageService.generateAnswer(mockPatient, "conv1", "Hello"))
                .thenReturn(msg);

        when(messageService.generateAnswer(mockPatient, "conv1", "Hello"))
                .thenReturn(msg);

        // Act & Assert
        mockMvc.perform(post("/patients/journal-entry-conversation/conv1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("m1"))
                .andExpect(jsonPath("$.conversationId").value("conv1"))
                .andExpect(jsonPath("$.requestMessage").value("Hello"))
                .andExpect(jsonPath("$.responseMessage").value("Hi there!"))
                .andExpect(jsonPath("$.timestamp").value(ts.toString()));
    }

    @Test
    void testGetAllMessages_ReturnsDecryptedJournalConversationOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String conversationId = "conv1";

        Patient patient = new Patient();
        patient.setPrivateKey("encKey");
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        JournalEntryConversation conv = new JournalEntryConversation();
        conv.setId(conversationId);
        conv.setConversationName("ChatSession");
        conv.setPatient(patient);

        Message m = new Message();
        m.setId("m2");
        m.setRequest("ENC_REQ");
        m.setResponse("ENC_RES");
        m.setConversation(conv);
        m.setExternalConversationId(conversationId);
        conv.getMessages().add(m);

        when(conversationService.getAllMessagesFromConversation(conversationId, patient))
                .thenReturn(conv);

        try (var crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey"))
                    .thenReturn("plainKey");
            crypto.when(() -> CryptographyUtil.decrypt("ENC_REQ", "plainKey"))
                    .thenReturn("DecryptedReq");
            crypto.when(() -> CryptographyUtil.decrypt("ENC_RES", "plainKey"))
                    .thenReturn("DecryptedRes");

            // Act
            JournalConversationOutputDTO dto = controller.getAllMessages(request, conversationId);

            // Assert top‐level fields
            assertEquals(conversationId, dto.getId());
            assertEquals("ChatSession", dto.getName());
            assertNotNull(dto.getMessages());
            assertEquals(1, dto.getMessages().size());

            // Assert the single message was decrypted and mapped
            MessageOutputDTO msgDto = dto.getMessages().get(0);
            assertEquals("DecryptedReq", msgDto.getRequestMessage());
            assertEquals("DecryptedRes", msgDto.getResponseMessage());
        }

        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(conversationService).getAllMessagesFromConversation(conversationId, patient);
        verifyNoMoreInteractions(patientService, conversationService);
    }

    @Test
    void deleteJournalChat_returnsOk() throws Exception {
        // Arrange
        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);
        doNothing().when(conversationService)
                .deleteAllMessagesFromConversation("conv2", mockPatient);

        // Act & Assert
        mockMvc.perform(delete("/patients/journal-entry-conversation/conv2"))
                .andExpect(status().isOk());
    }
}
