package ch.uzh.ifi.imrg.patientapp.rest.dto.output.document;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;

@Getter
@Setter
public class DocumentConversationOutputDTO {
    private String id;
    private List<MessageOutputDTO> messages;
}
