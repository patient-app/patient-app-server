package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageOutputDTO {
    private String timestamp;
    private String id;
    private String conversationId;
    private String responseMessage;
    private String requestMessage;
}
