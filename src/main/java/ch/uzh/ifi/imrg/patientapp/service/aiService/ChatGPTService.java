package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.utils.EnvironmentVariables;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChatGPTService {
    boolean USE_CHATGPT = false;
    private final RestTemplate restTemplate;
    private static final String LOCAL_AI_API_URL = "https://vllm-imrg.ifi.uzh.ch/v1/chat/completions";

    private static final List<String> predefinedResponses = List.of(
            "Sure, let's discuss it!",
            "That's an intriguing observation.",
            "Can you explain that a bit more?",
            "Hmmm, that's not something I expected.",
            "That's a very valid concern. Let's unpack it together.",
            "I see where you're coming from, but I'm not sure I agree entirely.",
            "Fascinating idea — it makes me think of something I read recently about human behavior and decision-making.",
            "I'm going to need a moment to process that. It's more complex than it appears at first glance.",
            "Wow, you're really making me think with that one. I appreciate how you challenge assumptions.",
            "Sometimes the best way to move forward is by questioning everything we believe to be true.",
            "That's a perspective I hadn't considered before. Let's break it down step by step and explore its implications.",
            "There's a lot to digest here. The topic you brought up connects to multiple other ideas in philosophy and logic.",
            "You're touching on a concept that has layers of nuance. Let's see if we can untangle them one at a time."
    );
    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public String getResponse(List<Map<String, String>> messages, boolean isAdmin){
        /*isAdmin = true;

        if (!USE_CHATGPT || !isAdmin){
            int randomIndex = ThreadLocalRandom.current().nextInt(predefinedResponses.size());
            return predefinedResponses.get(randomIndex);
        }
        */
        return callAPI(messages);
    }
    public String callAPI(List<Map<String, String>> messages) {
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
                        String text = (String) message.get("content"); // ✅ FIXED
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
    /* Single string Implementation
    private String callAPI(String messageRequest){
        String content = "content";
        // Prepare the request bodyRequest
        Map<String, Object> bodyRequest = new HashMap<>();
        bodyRequest.put("model", "Qwen/Qwen2.5-1.5B-Instruct");
        bodyRequest.put("prompt", messageRequest);
        bodyRequest.put("max_tokens", 70);
        bodyRequest.put("temperature", 0);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(EnvironmentVariables.getLocalLlmApiKey());
        System.out.println("Chello?");
        System.out.println(bodyRequest);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(LOCAL_AI_API_URL, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    String text = (String) firstChoice.get("text"); // FIXED
                    if (text != null && !text.isEmpty()) {
                        System.out.println("Returned response:");
                        System.out.println(text);
                        return text;
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

     */

}
