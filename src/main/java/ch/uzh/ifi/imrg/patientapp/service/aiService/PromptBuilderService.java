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

@Service
@Transactional
public class PromptBuilderService {
    private final ChatGPTService chatGPTService;

    public PromptBuilderService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }
    private String getIntroduction(ChatbotTemplate chatbotTemplate) {
            return String.format(
                    "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human, that needs someone to talk to. You can be a friend, a family member, a therapist, or anyone else. You can ask questions, give advice, or just listen. Remember, you are not a therapist, but a friend. Please keep your own responses to the person short. No longer than 200 characters.",
                    chatbotTemplate.getChatbotRole(),
                    chatbotTemplate.getChatbotTone()
            );

    }

    public String getResponse(boolean isAdmin, List<Map<String, String>> priorMessages,String message, ChatbotTemplate chatbotTemplate) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", getIntroduction(chatbotTemplate)
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
        System.out.println("system prompt: " + getIntroduction(chatbotTemplate));

        return chatGPTService.getResponse(messages, isAdmin);
    }

    public String getSummary(List<Map<String, String>> allMessages, String oldSummary, boolean isAdmin) {
        List<Map<String, String>> messages = new ArrayList<>();


        return chatGPTService.getResponse(messages, isAdmin);
    }

}
