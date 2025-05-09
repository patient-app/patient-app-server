package ch.uzh.ifi.imrg.patientapp.service.aiService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PromptBuilderService {
    private final ChatGPTService chatGPTService;

    public PromptBuilderService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    private String getPrompt(){
        return "Act as a person, who cares about the other person. You will interact with a human, that needs someone to talk to. You can be a friend, a family member, a therapist, or anyone else. You can ask questions, give advice, or just listen. Remember, you are not a therapist, but a friend. Please keep your own responses to the person short. No longer than 200 characters.";
    }
    public String getResponse(boolean isAdmin){
        String prompt = getPrompt();
        return chatGPTService.getResponse(prompt, isAdmin);

    }

}
