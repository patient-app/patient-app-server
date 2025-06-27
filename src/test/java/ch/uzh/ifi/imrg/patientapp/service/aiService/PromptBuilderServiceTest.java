package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PromptBuilderServiceTest {
    @Mock
    private ChatGPTService chatGPTService;
    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;
    @InjectMocks
    private PromptBuilderService promptBuilderService;


    @Test
    void getResponse_shouldCallChatGPTServiceWithExpectedPrompt() {
        // Arrange
        String mockResponse = "Hi there!";

        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("compassionate assistant");

        when(chatGPTService.getResponse(anyList(), eq(false)))
                .thenReturn(mockResponse);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hello?")
        );

        // Act
        String actualResponse = promptBuilderService.getResponse(false, messages, "hi", template);

        // Assert
        assertEquals(mockResponse, actualResponse);
    }


    @Test
    void getResponse_shouldCallChatGPTServiceWithNoPriorMessages() {
        // Arrange
        String mockResponse = "Hi there!";

        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("compassionate assistant");

        when(chatGPTService.getResponse(anyList(), eq(false)))
                .thenReturn(mockResponse);

        // Act
        String actualResponse = promptBuilderService.getResponse(false, null, "hi", template);

        // Assert
        assertEquals(mockResponse, actualResponse);
    }

}
