package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.utils.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatGPTServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatGPTService chatGPTService;

    @Test
    void testCallAPI_validResponse_returnsContent() {
        // Arrange
        try (MockedStatic<EnvironmentVariables> envMock = Mockito.mockStatic(EnvironmentVariables.class)) {
            envMock.when(EnvironmentVariables::getLocalLlmApiKey).thenReturn("fake-key");

        }
        Map<String, Object> message = Map.of("content", "Hello!");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> responseBody = Map.of("choices", List.of(choice));
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);


        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", "Hi2"));

        // Act
        String result = chatGPTService.callAPI(messages);
        System.out.println(result);
        // Assert
        assertEquals("Hello!", result);
    }

    @Test
    void testCallAPI_emptyChoices_returnsFallback() {
        Map<String, Object> responseBody = Map.of("choices", List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        String result = chatGPTService.callAPI(List.of(Map.of("role", "user", "content", "Hi")));
        System.out.println(result);
        assertEquals("No content found in LLM response.", result);
    }

    @Test
    void testCallAPI_nonOKStatus_returnsError() {
        ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        String result = chatGPTService.callAPI(List.of(Map.of("role", "user", "content", "Hi")));
        System.out.println(result);
        assertEquals("OpenAI API returned non-OK status: 400 BAD_REQUEST", result);
    }

    @Test
    void testCallAPI_exceptionThrown_returnsErrorMessage() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        String result = chatGPTService.callAPI(List.of(Map.of("role", "user", "content", "Hi")));
        System.out.println(result);
        assertEquals("Error calling OpenAI API: Connection refused", result);
    }
}

