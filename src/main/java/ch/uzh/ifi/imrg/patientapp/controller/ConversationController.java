package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutOnboardedDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ConversationController {
    private final PatientService patientService;
    private final ConversationService conversationService;
    private final MessageService messageService;


    ConversationController(PatientService patientService,
                           ConversationService conversationService,
                           MessageService messageService) {
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

    @PutMapping("/patients/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateSharing(@RequestBody PutSharingDTO putSharingDTO, @PathVariable String conversationId, HttpServletRequest httpServletRequest){
        System.out.println("hi");
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        conversationService.updateSharing(putSharingDTO, conversationId, loggedInPatient);
    }

    @GetMapping("/patients/conversations/{patientId}")
    @ResponseStatus(HttpStatus.OK)
    public List<NameConversationOutputDTO> nameConversationDTO(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        List<Conversation> conversationList = conversationService.getAllConversationsFromPatient(loggedInPatient);
        return ConversationMapper.INSTANCE.convertEntityListToNameConversationOutputDTOList(conversationList);
    }

    @PostMapping("/patients/conversations/messages/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                                        @RequestBody CreateMessageDTO createMessageDTO,
                                        @PathVariable String conversationId) throws AccessDeniedException {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId, createMessageDTO.getMessage());

        return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
    }

    @GetMapping("/patients/conversations/messages/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public CompleteConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                                                       @PathVariable String conversationId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Conversation completeConversation = conversationService.getAllMessagesFromConversation(conversationId, loggedInPatient);
        CompleteConversationOutputDTO completeConversationOutputDTO = ConversationMapper.INSTANCE.convertEntityToCompleteConversationOutputDTO(completeConversation);
        completeConversationOutputDTO.setMessages(new ArrayList<>());

        for(Message message : completeConversation.getMessages()) {
            message.setResponse(CryptographyUtil.decrypt(message.getResponse(),CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            message.setRequest(CryptographyUtil.decrypt(message.getRequest(),CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
            completeConversationOutputDTO.getMessages().add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
        }
        return completeConversationOutputDTO;

    }

    @DeleteMapping("/patients/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteChat(HttpServletRequest httpServletRequest,
                           @PathVariable String conversationId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        conversationService.deleteConversation(conversationId, loggedInPatient);

    }
}
