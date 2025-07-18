package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutConversationNameDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.ChatbotService;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ConversationController {
        private final PatientService patientService;
        private final ConversationService conversationService;
        private final MessageService messageService;
        private final ChatbotService chatbotService;

        ConversationController(PatientService patientService,
                        ConversationService conversationService,
                        MessageService messageService, ChatbotService chatbotService) {
                this.patientService = patientService;
                this.conversationService = conversationService;
                this.messageService = messageService;

                this.chatbotService = chatbotService;
        }

        @PostMapping("/patients/conversations")
        @ResponseStatus(HttpStatus.CREATED)
        public CreateConversationOutputDTO createConversation(HttpServletRequest httpServletRequest) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                GeneralConversation createdConversation = conversationService.createConversation(loggedInPatient);
                // add the conversation to the patient
                patientService.addConversationToPatient(loggedInPatient, createdConversation);
                return new CreateConversationOutputDTO(createdConversation.getId(),
                                chatbotService.getWelcomeMessage(loggedInPatient.getId()),
                                loggedInPatient.getChatBotAvatar());
        }

        @PutMapping("/patients/conversations/{conversationId}")
        @ResponseStatus(HttpStatus.OK)
        public void updateSharing(@RequestBody PutSharingDTO putSharingDTO, @PathVariable String conversationId,
                        HttpServletRequest httpServletRequest) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                conversationService.updateSharing(putSharingDTO, conversationId, loggedInPatient);
        }

        @PutMapping("/patients/conversations/{conversationId}/conversation-name")
        @ResponseStatus(HttpStatus.OK)
        public void postConversationName(@RequestBody PutConversationNameDTO putConversationNameDTO,
                        @PathVariable String conversationId,
                        HttpServletRequest httpServletRequest) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                conversationService.setConversationName(putConversationNameDTO, conversationId, loggedInPatient);
        }

        @GetMapping("/patients/conversations")
        @ResponseStatus(HttpStatus.OK)
        public List<NameConversationOutputDTO> getConversationNames(HttpServletRequest httpServletRequest) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                List<GeneralConversation> conversationList = conversationService
                                .getAllConversationsFromPatient(loggedInPatient);
                return ConversationMapper.INSTANCE.convertEntityListToNameConversationOutputDTOList(conversationList);
        }

        @PostMapping("/patients/conversations/messages/{conversationId}")
        @ResponseStatus(HttpStatus.OK)
        public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                        @RequestBody CreateMessageDTO createMessageDTO,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId,
                                createMessageDTO.getMessage());
                return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
        }

        @GetMapping("/patients/conversations/messages/{conversationId}")
        @ResponseStatus(HttpStatus.OK)
        public CompleteConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                Conversation completeConversation = conversationService.getAllMessagesFromConversation(
                                conversationId,
                                loggedInPatient);
                GeneralConversation generalConversation = (GeneralConversation) completeConversation;

                CompleteConversationOutputDTO completeConversationOutputDTO = ConversationMapper.INSTANCE
                                .convertEntityToCompleteConversationOutputDTO(generalConversation);
                completeConversationOutputDTO.setMessages(new ArrayList<>());

                for (Message message : completeConversation.getMessages()) {
                        message.setResponse(CryptographyUtil.decrypt(message.getResponse(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        message.setRequest(CryptographyUtil.decrypt(message.getRequest(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        completeConversationOutputDTO.getMessages()
                                        .add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
                }

                completeConversationOutputDTO.setChatBotAvatar(loggedInPatient.getChatBotAvatar());

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
