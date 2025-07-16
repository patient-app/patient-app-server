package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.utils.EnvironmentVariables;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatGPTService {
    private final RestTemplate restTemplate;
    private static final String LOCAL_AI_API_URL = "https://vllm-imrg.ifi.uzh.ch/vllm/v1/chat/completions";

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String getResponse(List<Map<String, String>> messages) {
        String content = "content";
        // Prepare the request bodyRequest
        Map<String, Object> bodyRequest = new HashMap<>();
        bodyRequest.put("model", "Qwen/Qwen3-1.7B");
        bodyRequest.put("messages", messages);
        //bodyRequest.put("max_tokens", 170);
        bodyRequest.put("temperature", 0);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(EnvironmentVariables.getLocalLlmApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(LOCAL_AI_API_URL, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message != null) {
                        String text = (String) message.get("content");
                        if (text != null && !text.isEmpty()) {
                            System.out.println("Returned response:");
                            System.out.println(text);
                            return text;
                        }
                    }
                }
                return "No content found in LLM response.";
            } else {
                return "OpenAI API returned non-OK status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI API: " + e.getMessage();
        }
    }

}
