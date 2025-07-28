package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
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
        //String mockResponse = "<think>thinking</think>Hi there!";
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
        assertEquals("Hi there!", actualResponse);
    }


    @Test
    void getResponse_shouldCallChatGPTServiceWithNoPriorMessages() {
        // Arrange
        //String mockResponse = "<think>thinking</think>Hi there!";
        String mockResponse = "Hi there!";
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
        Patient patient = new Patient();
        patient.setLanguage("en");

        // Act
        String result = promptBuilderService.getSystemPrompt(template, patient);

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
        Patient patient = new Patient();
        patient.setLanguage("en");

        // Act
        String result = promptBuilderService.getSystemPrompt(template, context,patient);

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
        //String mockResponse = "<think>thinking</think>Summary text.";
        String mockResponse = "Summary text.";
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
        //String mockResponse = "<think>thinking</think>Summary text";
        String mockResponse = "Summary text";
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
        //String mockResponse = "<think>thinking</think>Updated summary text.";
        String mockResponse = "Updated summary text.";
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
        //String mockResponse = "<think>thinking</think>Updated summary.";
        String mockResponse = "Updated summary.";
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
        //String mockResponse = "<think>thinking</think>true";
        String mockResponse = "true";
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

    @Test
    void getSummaryOfAllConversations_shouldCombineMultipleSummaries() {
        // Arrange
        List<String> summaries = List.of(
                "First conversation summary.",
                "Second conversation summary.",
                "Third conversation summary."
        );

        //String mockResponse = "<think>thinking</think>This is the overall summary.";
        String mockResponse = "This is the overall summary.";
        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummaryOfAllConversations(summaries);

        // Assert
        assertEquals("This is the overall summary.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            // Should have exactly 2 messages
            if (messages.size() != 2) return false;
            Map<String, String> sys = messages.get(0);
            Map<String, String> user = messages.get(1);

            // System prompt correct
            if (!sys.get("content").contains("summarizes multiple conversation summaries")) return false;
            if (!sys.get("role").equals("system")) return false;

            // User prompt contains all summaries numbered
            String content = user.get("content");
            return content.contains("Summary 1: First conversation summary.")
                    && content.contains("Summary 2: Second conversation summary.")
                    && content.contains("Summary 3: Third conversation summary.");
        }));
    }

    @Test
    void getSummaryOfAllConversations_shouldHandleSingleSummary() {
        // Arrange
        List<String> summaries = List.of("Only one conversation summary.");

        //String mockResponse = "<think>thinking</think>Single summary result.";
        String mockResponse = "Single summary result.";
        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummaryOfAllConversations(summaries);

        // Assert
        assertEquals("Single summary result.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            if (messages.size() != 2) return false;
            Map<String, String> user = messages.get(1);
            String content = user.get("content");
            return content.contains("Summary 1: Only one conversation summary.");
        }));
    }

    @Test
    void getSummaryOfAllConversations_shouldHandleEmptySummaries() {
        // Arrange
        List<String> summaries = List.of();

        //String mockResponse = "<think>thinking</think>No summaries provided.";
        String mockResponse = "No summaries provided.";
        when(chatGPTService.getResponse(anyList()))
                .thenReturn(mockResponse);

        // Act
        String result = promptBuilderService.getSummaryOfAllConversations(summaries);

        // Assert
        assertEquals("No summaries provided.", result);

        verify(chatGPTService).getResponse(argThat(messages -> {
            if (messages.size() != 2) return false;
            Map<String, String> user = messages.get(1);
            String content = user.get("content");
            // Should still include the label even though the StringBuilder is empty
            return content.contains("Here are the conversation summaries:");
        }));
    }
/*
    @Test
    void getSummary_shouldThrowWhenNoThinkClosingTag() {
        // Arrange
        List<Map<String, String>> priorMessages = List.of(
                Map.of("role", "user", "content", "Hello, are you there?")
        );
        // Response WITHOUT </think>
        String invalidResponse = "This is some malformed response without closing tag.";

        when(chatGPTService.getResponse(anyList()))
                .thenReturn(invalidResponse);

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> promptBuilderService.getSummary(priorMessages, null)
        );

        assertTrue(ex.getMessage().contains("No <think> closing tag found in response"));
        assertTrue(ex.getMessage().contains("This is some malformed response"));
    }*/

    @Test
    void getJournalSystemPrompt_shouldContainAllInputs() {
        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("therapist");
        template.setChatbotTone("empathetic");
        Patient patient = new Patient();
        patient.setLanguage("en");

        String title = "My Day";
        String content = "Today I felt overwhelmed by work.";

        String result = promptBuilderService.getJournalSystemPrompt(template, title, content, patient);

        assertTrue(result.contains("therapist"));
        assertTrue(result.contains("empathetic"));
        assertTrue(result.contains("My Day"));
        assertTrue(result.contains("Today I felt overwhelmed by work."));
        assertTrue(result.length() < 1000); // sanity check for length
    }

    @Test
    void getDocumentSystemPrompt_shouldContainAllInputs() {
        ChatbotTemplate template = new ChatbotTemplate();
        template.setChatbotRole("coach");
        template.setChatbotTone("supportive");
        Patient patient = new Patient();
        patient.setLanguage("en");
        String document = "This is a patient summary with important information.";

        String result = promptBuilderService.getDocumentSystemPrompt(template, document, patient);

        assertTrue(result.contains("coach"));
        assertTrue(result.contains("supportive"));
        assertTrue(result.contains(document));
        assertTrue(result.length() < 1000); // sanity check for length
    }

}
