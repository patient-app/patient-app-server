package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import ch.uzh.ifi.imrg.patientapp.constant.ChatBotAvatar;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreateConversationOutputDTO {
    private String id;
    private String welcomeMessage;
    private ChatBotAvatar chatBotAvatar;
}
