package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class GetConversationSummaryInputDTO {
    @NotNull
    private Instant start;

    @NotNull
    private Instant end;
}
