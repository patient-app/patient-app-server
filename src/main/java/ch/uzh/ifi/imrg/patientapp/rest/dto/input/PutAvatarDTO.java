package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import ch.uzh.ifi.imrg.patientapp.constant.ChatBotAvatar;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutAvatarDTO {

    @NotNull(message = "chatBotAvatar must be provided")
    private ChatBotAvatar chatBotAvatar;

    public PutAvatarDTO() {
    }
}
