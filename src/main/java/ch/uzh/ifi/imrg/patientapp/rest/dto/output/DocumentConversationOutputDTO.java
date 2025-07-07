package ch.uzh.ifi.imrg.patientapp.rest.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentConversationOutputDTO {
    private String id;
    private List<MessageOutputDTO> messages;
}
