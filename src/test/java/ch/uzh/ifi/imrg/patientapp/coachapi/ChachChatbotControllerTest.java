package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ChachChatbotControllerTest {
    @Mock
    private ChatbotService chatbotService;

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
}
