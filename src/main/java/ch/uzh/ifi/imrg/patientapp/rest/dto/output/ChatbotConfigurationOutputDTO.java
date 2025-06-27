package ch.uzh.ifi.imrg.patientapp.rest.dto.output;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotConfigurationOutputDTO {
    private String id;
    private boolean isActive;
    private String chatbotRole;
    private String chatbotTone;
    private String welcomeMessage;
}
