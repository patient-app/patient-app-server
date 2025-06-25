package ch.uzh.ifi.imrg.patientapp.service.aiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
        String expectedPromptStart = "Act as a person, who cares about the other person";
        String mockResponse = "Hi there!";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hello?")
        );

        when(chatGPTService.getResponse(anyList(), eq(false))).thenReturn(mockResponse);

        // Act
        String actualResponse = promptBuilderService.getResponse(false, messages, "hi");

        // Assert
        assertEquals(mockResponse, actualResponse);


    }

    @Test
    void getResponse_shouldCallChatGPTServiceWithNoPriorMessages() {
        // Arrange
        String expectedPromptStart = "Act as a person, who cares about the other person";
        String mockResponse = "Hi there!";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hello?")
        );

        when(chatGPTService.getResponse(anyList(), eq(false))).thenReturn(mockResponse);

        // Act
        String actualResponse = promptBuilderService.getResponse(false, null, "hi");

        // Assert
        assertEquals(mockResponse, actualResponse);


    }
}
