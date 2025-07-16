package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.GetConversationSummaryInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ConversationSummaryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ChachChatbotControllerTest {
    @Mock
    private ChatbotService chatbotService;

    @Autowired
    private MockMvc mockMvc;


    @InjectMocks
    private CoachChatbotController coachChatbotController;

    @Test
    void testCreateChatbot_CallsChatbotService() {
        // Arrange
        String patientId = "patient123";
        CreateChatbotDTO createChatbotDTO = new CreateChatbotDTO();

        // Act
        coachChatbotController.createChatbot(patientId, createChatbotDTO);

        // Assert
        verify(chatbotService).createChatbot(patientId, createChatbotDTO);
        verifyNoMoreInteractions(chatbotService);
    }

    @Test
    void testGetChatbotConfigurations_ReturnsConfigurations() {
        // Arrange
        String patientId = "patient123";
        ChatbotConfigurationOutputDTO dto1 = new ChatbotConfigurationOutputDTO();
        ChatbotConfigurationOutputDTO dto2 = new ChatbotConfigurationOutputDTO();
        List<ChatbotConfigurationOutputDTO> expected = List.of(dto1, dto2);
        when(chatbotService.getChatbotConfigurations(patientId)).thenReturn(expected);

        // Act
        List<ChatbotConfigurationOutputDTO> result = coachChatbotController.getChatbotConfigurations(patientId);

        // Assert
        assertEquals(expected, result);
        verify(chatbotService).getChatbotConfigurations(patientId);
        verifyNoMoreInteractions(chatbotService);
    }

    @Test
    void testUpdateChatbot_CallsUpdateChatbotOnService() {
        // Arrange
        String patientId = "patient123";
        UpdateChatbotDTO updateChatbotDTO = new UpdateChatbotDTO();

        // Act
        coachChatbotController.updateChatbot(patientId, updateChatbotDTO);

        // Assert
        verify(chatbotService).updateChatbot(patientId, updateChatbotDTO);
        verifyNoMoreInteractions(chatbotService);
    }

    @Test
    void testGetConversationSummary_CallsServiceAndReturnsDTO() {
        // Arrange
        String patientId = "patient123";
        GetConversationSummaryInputDTO inputDTO = new GetConversationSummaryInputDTO();
        ConversationSummaryOutputDTO expected = new ConversationSummaryOutputDTO();
        expected.setConversationSummary("This is a test summary.");

        when(chatbotService.getConversationSummary(patientId, inputDTO))
                .thenReturn(expected);

        // Act
        ConversationSummaryOutputDTO result = coachChatbotController.getConversationSummary(inputDTO, patientId);

        // Assert
        assertEquals(expected, result);
        verify(chatbotService).getConversationSummary(patientId, inputDTO);
        verifyNoMoreInteractions(chatbotService);
    }

    @Test
    void testHandleIllegalState_returnsMessage() {
        // Arrange
        String errorMessage = "Chatbot already exists";
        IllegalStateException ex = new IllegalStateException(errorMessage);

        // Act
        String result = coachChatbotController.handleIllegalState(ex);

        // Assert
        assertEquals(errorMessage, result);
    }

}
