package ch.uzh.ifi.imrg.patientapp.service.aiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PromptBuilderServiceTest {
    private ChatGPTService chatGPTService;
    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        chatGPTService = mock(ChatGPTService.class);
        promptBuilderService = new PromptBuilderService(chatGPTService);
    }

    @Test
    void getResponse_shouldCallChatGPTServiceWithExpectedPrompt() {
        // Arrange
        String expectedPromptStart = "Act as a person, who cares about the other person";
        String mockResponse = "Hi there!";
        when(chatGPTService.getResponse(anyString(),false)).thenReturn(mockResponse);

        // Act
        String response = promptBuilderService.getResponse(true,"");

        // Assert
        assertEquals(mockResponse, response);
        verify(chatGPTService, times(1)).getResponse(argThat(prompt ->
                prompt.startsWith(expectedPromptStart) && prompt.length() < 1000), eq(false));
    }
}
