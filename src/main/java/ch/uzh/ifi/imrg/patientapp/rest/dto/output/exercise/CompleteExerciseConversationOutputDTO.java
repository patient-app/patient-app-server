package ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise;


import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompleteExerciseConversationOutputDTO {
    private String id;
    private String name;
    private List<MessageOutputDTO> messages;
}
