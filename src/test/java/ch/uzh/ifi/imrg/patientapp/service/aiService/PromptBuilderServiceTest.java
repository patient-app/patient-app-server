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
        String mockResponse = "<think>thinking</think>Hi there!";

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
        assertEquals("Hi there!", actualResponse);
    }


    @Test
    void getResponse_shouldCallChatGPTServiceWithNoPriorMessages() {
        // Arrange
        String mockResponse = "<think>thinking</think>Hi there!";

        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("compassionate assistant");

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String actualResponse = promptBuilderService.getResponse( null, "hi", anyString());

        // Assert
        assertEquals("Hi there!", actualResponse);
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

    @Test
    void getSummary_whenOldSummaryIsNull_andMessagesProvided_shouldUseSummarizePrompt() {
        // Arrange
        List<Map<String, String>> priorMessages = List.of(
                Map.of("role", "user", "content", "Hello, how are you?")
        );
        String mockResponse = "<think>thinking</think>Summary text.";

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummary(priorMessages, null);

        // Assert
        assertEquals("Summary text.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            // Should contain the system prompt for summarizing
            String sys = messages.get(0).get("content");
            return sys.contains("Summarize the following conversation");
        }));
    }

    @Test
    void getSummary_whenOldSummaryIsBlank_andMessagesNull_shouldUseSummarizePromptAndOnlySystemMessage() {
        // Arrange
        String mockResponse = "<think>thinking</think>Summary text";

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummary(null, "   ");

        // Assert
        assertEquals("Summary text", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            // Only one system prompt message
            return messages.size() == 1 &&
                    messages.get(0).get("content").contains("Summarize the following conversation");
        }));
    }

    @Test
    void getSummary_whenOldSummaryPresent_andMessagesProvided_shouldUseUpdatePrompt() {
        // Arrange
        String oldSummary = "This is the old summary.";
        List<Map<String, String>> priorMessages = List.of(
                Map.of("role", "assistant", "content", "I am fine, thank you.")
        );
        String mockResponse = "<think>thinking</think>Updated summary text.";

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummary(priorMessages, oldSummary);

        // Assert
        assertEquals("Updated summary text.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            String sys = messages.get(0).get("content");
            return sys.contains("Update the following existing summary") &&
                    sys.contains(oldSummary);
        }));
    }

    @Test
    void getSummary_whenOldSummaryPresent_andMessagesNull_shouldUseUpdatePromptAndOnlySystemMessage() {
        // Arrange
        String oldSummary = "Prior conversation summary.";
        String mockResponse = "<think>thinking</think>Updated summary.";
        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummary(null, oldSummary);

        // Assert
        assertEquals("Updated summary.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            // Only the system prompt message
            return messages.size() == 1 &&
                    messages.get(0).get("content").contains("Update the following existing summary") &&
                    messages.get(0).get("content").contains(oldSummary);
        }));
    }

    @Test
    void getHarmRating_shouldCallChatGPTServiceWithExpectedPrompt() {
        // Arrange
        String inputMessage = "I want to end my life.";
        String mockResponse = "<think>thinking</think>true";

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getHarmRating(inputMessage);

        // Assert
        assertEquals("true", result);

        // Verify the prompt contents
        verify(chatGPTService).getResponse(argThat(messages -> {
            // Should contain exactly 2 messages
            if (messages.size() != 2) return false;

            Map<String, String> sysMsg = messages.get(0);
            Map<String, String> userMsg = messages.get(1);

            return sysMsg.get("role").equals("system")
                    && sysMsg.get("content").contains("classifier that detects suicide risk")
                    && userMsg.get("role").equals("user")
                    && userMsg.get("content").equals(inputMessage);
        }));
    }




}
