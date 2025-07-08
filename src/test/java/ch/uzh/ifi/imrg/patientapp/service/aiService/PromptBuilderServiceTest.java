package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PromptBuilderServiceTest {
    @Mock
    private ChatGPTService chatGPTService;

    @InjectMocks
    private PromptBuilderService promptBuilderService;


    @Test
    void getResponse_shouldCallChatGPTServiceWithExpectedPrompt() {
        // Arrange
        String mockResponse = "Hi there!";

        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("compassionate assistant");

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hello?")
        );

        // Act
        String actualResponse = promptBuilderService.getResponse( messages, "hi", anyString());

        // Assert
        assertEquals(mockResponse, actualResponse);
    }


    @Test
    void getResponse_shouldCallChatGPTServiceWithNoPriorMessages() {
        // Arrange
        String mockResponse = "Hi there!";

        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("compassionate assistant");

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String actualResponse = promptBuilderService.getResponse( null, "hi", anyString());

        // Assert
        assertEquals(mockResponse, actualResponse);
    }


    @Test
    void getSystemPrompt_withoutContext_returnsFormattedString() {
        // Arrange
        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("friendly coach");
        template.setChatbotTone("encouraging");

        // Act
        String result = promptBuilderService.getSystemPrompt(template);

        // Assert
        assertTrue(result.contains("Act as a friendly coach, who cares about the other person."));
        assertTrue(result.contains("Your tone should be encouraging."));
        assertTrue(result.contains("No longer than 200 characters."));
    }

    @Test
    void getSystemPrompt_withContext_returnsFormattedStringIncludingContext() {
        // Arrange
        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("helpful guide");
        template.setChatbotTone("warm");
        String context = "This exercise helps you reflect on gratitude.";

        // Act
        String result = promptBuilderService.getSystemPrompt(template, context);

        // Assert
        assertTrue(result.contains("Act as a helpful guide, who cares about the other person."));
        assertTrue(result.contains("Your tone should be warm."));
        assertTrue(result.contains(context));
        assertTrue(result.contains("No longer than 400 characters."));
    }


}
