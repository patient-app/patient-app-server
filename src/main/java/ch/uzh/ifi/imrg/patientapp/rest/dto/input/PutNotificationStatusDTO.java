package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutNotificationStatusDTO {

    @NotNull
    private boolean getNotifications;
}
