package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PromptBuilderService {
    private final ChatGPTService chatGPTService;

    public PromptBuilderService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    public String getSystemPrompt(ChatbotTemplate chatbotTemplate) {
            return String.format(
                    "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human, that needs someone to talk to. You can be a friend, a family member, a therapist, or anyone else. You can ask questions, give advice, or just listen. Remember, you are not a therapist, but a friend. Please keep your own responses to the person short. No longer than 200 characters.",
                    chatbotTemplate.getChatbotRole(),
                    chatbotTemplate.getChatbotTone()
            );

    }
    public String getSystemPrompt(ChatbotTemplate chatbotTemplate, String context) {
        return String.format(
                "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human, that needs someone to talk to. You should help the person to understand this exercise: %s When answering questions about the exercise only use the description from before. It is really important to stick to this description. Please keep your own responses to the person short. No longer than 400 characters.",
                chatbotTemplate.getChatbotRole(),
                chatbotTemplate.getChatbotTone(),
                context
        );

    }

    public String getResponse(List<Map<String, String>> priorMessages,String message, String systemPrompt) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));

        // Add prior chat history
        if (priorMessages != null) {
            messages.addAll(priorMessages);
        }

        // Current user message
        messages.add(Map.of(
                "role", "user",
                "content", message
        ));

        return chatGPTService.getResponse(messages);
    }

    public String getSummary(List<Map<String, String>> allMessages, String oldSummary) {
        List<Map<String, String>> messages = new ArrayList<>();

        String systemPrompt;

        if (oldSummary == null || oldSummary.isBlank()) {
            systemPrompt = "Summarize the following conversation in a few sentences. "
                    + "Do not use bullet points or lists. Just write a short summary of the conversation. "
                    + "The summary should be no longer than 200 characters.\n"
                    + "Messages: ";
        } else {
            systemPrompt = "Update the following existing summary by including the additional messages below. "
                    + "Keep it short (no more than 200 additional characters), do not use bullet points or lists.\n\n"
                    + "Existing summary:\n" + oldSummary + "\n\n"
                    + "Additional messages:\n";
        }

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));

        // Add prior chat history
        if (allMessages != null) {
            messages.addAll(allMessages);
        }

        return chatGPTService.getResponse(messages);
    }
    public String getHarmRating(String message) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", "You are a classifier that detects suicide risk and self harm in text messages. Respond ONLY with 'true' or 'false'. If either appear Respond with 'true'."
        ));

        // Current user message
        messages.add(Map.of(
                "role", "user",
                "content", message
        ));

        return chatGPTService.getResponse(messages);
    }

    public String extractContentFromResponse(String rawAnswer) {
        // extract the answer part from the response
        String regex = "</think>\\s*([\\s\\S]*)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(rawAnswer);

        String answer;

        if (matcher.find()) {
            answer = matcher.group(1).trim();
        } else {
            throw new IllegalStateException("No <think> closing tag found in response:\n" + rawAnswer);
        }
        return answer;
    }

}
