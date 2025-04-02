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
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
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
    public String getResponse(String message){
        if (!USE_CHATGPT){
            int randomIndex = ThreadLocalRandom.current().nextInt(predefinedResponses.size());
            return predefinedResponses.get(randomIndex);
        }

        return callAPI(message);
    }
    private String callAPI(String messageRequest){
        String content = "content";
        // Prepare the request bodyRequest
        Map<String, Object> bodyRequest = new HashMap<>();
        bodyRequest.put("model", "gpt-4o-mini");
        bodyRequest.put("messages", List.of(
                Map.of("role", "user", content, messageRequest)
        ));

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(EnvironmentVariables.getChatGptApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                //System.out.println("Model used: " + body.get("model"));
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message != null && message.containsKey(content)) {
                        return (String) message.get(content);
                    }
                }
                return "No content found in OpenAI response.";
            } else {
                return "OpenAI API returned non-OK status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI API: " + e.getMessage();
        }


    }

}
