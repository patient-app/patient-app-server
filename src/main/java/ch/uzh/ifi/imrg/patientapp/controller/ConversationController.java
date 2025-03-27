package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
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
public class ConversationController {
    private final PatientService patientService;
    private final ConversationService conversationService;
    private final MessageService messageService;

    ConversationController(PatientService patientService,
                           ConversationService conversationService, MessageService messageService) {
        this.patientService = patientService;
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @PostMapping("/patients/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateConversationOutputDTO createConversation(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Conversation createdConversation = conversationService.createConversation(loggedInPatient);
        // add the conversation to the patient
        patientService.addConversationToPatient(loggedInPatient, createdConversation);
        return new CreateConversationOutputDTO(createdConversation.getExternalId());
    }

    @PostMapping("/patients/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                                        @RequestBody CreateMessageDTO createMessageDTO,
                                        @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId, createMessageDTO.getMessage());

        return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
    }

    @GetMapping("/patients/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public CompleteConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                                                       @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Conversation completeConversation = conversationService.getAllMessagesFromConversation(conversationId);
        CompleteConversationOutputDTO completeConversationOutputDTO = new CompleteConversationOutputDTO();
        completeConversationOutputDTO.setId(completeConversation.getExternalId());
        completeConversationOutputDTO.setMessages(new ArrayList<>());

        for(Message message : completeConversation.getMessages()) {
            System.out.println("message: " +message);
        }

        for(Message message : completeConversation.getMessages()) {
            message.setResponse(CryptographyUtil.decrypt(message.getResponse(),CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            message.setRequest(CryptographyUtil.decrypt(message.getRequest(),CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            completeConversationOutputDTO.getMessages().add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
            System.out.println("hi");
        }
        return completeConversationOutputDTO;

    }
}
