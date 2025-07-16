package ch.uzh.ifi.imrg.patientapp.rest.dto.input;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChatbotDTO {

    private String chatbotRole;
    private String chatbotTone;
    private String welcomeMessage;
}
