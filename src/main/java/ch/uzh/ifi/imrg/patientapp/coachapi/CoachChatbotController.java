package ch.uzh.ifi.imrg.patientapp.coachapi;



import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ChatbotService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CoachChatbotController {
    private final ChatbotService chatbotService;

    public CoachChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }


    @PostMapping("/coach/patients/{patientId}/chatbot")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public void createChatbot(@PathVariable String patientId, @RequestBody CreateChatbotDTO createChatbotDTO) {
        chatbotService.createChatbot(patientId, createChatbotDTO);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIllegalState(IllegalStateException ex) {
        return ex.getMessage();
    }

    @GetMapping("/coach/patients/{patientId}/chatbot")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ChatbotConfigurationOutputDTO> getChatbotConfigurations(@PathVariable String patientId) {
        return chatbotService.getChatbotConfigurations(patientId);
    }
    @PutMapping("/coach/patients/{patientId}/chatbot")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public void updateChatbot(@PathVariable String patientId, @RequestBody UpdateChatbotDTO updateChatbotDTO) {
        chatbotService.updateChatbot(patientId, updateChatbotDTO);
    }

    @GetMapping("/coach/patients/{patientId}/chatbot-summary")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public void getConversationSummary(@PathVariable String patientId) {
        chatbotService.getConversationSummary(patientId);
    }



}
