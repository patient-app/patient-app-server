package ch.uzh.ifi.imrg.patientapp.service.aiService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PromptBuilderService {
    private final ChatGPTService chatGPTService;

    public PromptBuilderService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    private String getIntroduction(){
        return "Act as a person, who cares about the other person. You will interact with a human, that needs someone to talk to. You can be a friend, a family member, a therapist, or anyone else. You can ask questions, give advice, or just listen. Remember, you are not a therapist, but a friend. Please keep your own responses to the person short. No longer than 200 characters.";
    }
    public String getResponse(boolean isAdmin, List<Map<String, String>> priorMessages,String message) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", getIntroduction()
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

        return chatGPTService.getResponse(messages, isAdmin);
    }


    /* Single string implementation
    public String getResponse(boolean isAdmin, String message){
        String prompt = getIntroduction();
        prompt += " " + message;
        System.out.println("Prompt:");
        System.out.println(prompt);
        return chatGPTService.getResponse(prompt, isAdmin);

    }
    */
}
