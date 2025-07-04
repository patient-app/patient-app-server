package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class ExerciseConversationController {
    private final PatientService patientService;
    private final MessageService messageService;
    private final ConversationService conversationService;

    public ExerciseConversationController(PatientService patientService, MessageService messageService, ConversationService conversationService) {
        this.patientService = patientService;
        this.messageService = messageService;
        this.conversationService = conversationService;
    }


    @PostMapping("/patients/exercises/chatbot/{conversationId}/messages")
    @ResponseStatus(HttpStatus.OK)
    public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                                        @RequestBody CreateMessageDTO createMessageDTO,
                                        @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId,
                createMessageDTO.getMessage());
        return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
    }

    @GetMapping("/patients/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.OK)
    public CompleteConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                                                        @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        GeneralConversation completeConversation = conversationService.getAllMessagesFromConversation(
                conversationId,
                loggedInPatient);
        CompleteConversationOutputDTO completeConversationOutputDTO = ConversationMapper.INSTANCE
                .convertEntityToCompleteConversationOutputDTO(completeConversation);
        completeConversationOutputDTO.setMessages(new ArrayList<>());

        for (Message message : completeConversation.getMessages()) {
            message.setResponse(CryptographyUtil.decrypt(message.getResponse(),
                    CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            message.setRequest(CryptographyUtil.decrypt(message.getRequest(),
                    CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            completeConversationOutputDTO.getMessages()
                    .add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
        }
        return completeConversationOutputDTO;

    }

    @DeleteMapping("/patients/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteChat(HttpServletRequest httpServletRequest,
                           @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        conversationService.deleteConversation(conversationId, loggedInPatient);
    }
}
