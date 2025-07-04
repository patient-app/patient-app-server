package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExerciseConversationController {
    private final PatientService patientService;
    private final MessageService messageService;

    public ExerciseConversationController(PatientService patientService, MessageService messageService) {
        this.patientService = patientService;
        this.messageService = messageService;
    }


    @PostMapping("/patients/exercises/{exerciseId}/chatbot/{conversationId}/messages")
    @ResponseStatus(HttpStatus.OK)
    public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                                        @RequestBody CreateMessageDTO createMessageDTO,
                                        @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId,
                createMessageDTO.getMessage());
        return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
    }
}
