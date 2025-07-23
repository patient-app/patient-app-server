package ch.uzh.ifi.imrg.patientapp.controller;

import java.util.ArrayList;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntryConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateJournalMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MessageMapper;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.MessageService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class JournalEntryConversationController {

        private final PatientService patientService;
        private final MessageService messageService;
        private final ConversationService conversationService;
        private final LogService logService;

        public JournalEntryConversationController(PatientService patientService, MessageService messageService,
                                                  ConversationService conversationService, LogService logService) {
                this.patientService = patientService;
                this.messageService = messageService;
                this.conversationService = conversationService;
                this.logService = logService;
        }

        @PostMapping("/patients/journal-entry-conversation/{conversationId}/messages")
        @ResponseStatus(HttpStatus.OK)
        public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                        @RequestBody CreateJournalMessageDTO createJournalMessageDTO,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

                conversationService.updateJournalConversationSystemPrompt(loggedInPatient, conversationId,
                                createJournalMessageDTO.getJournalEntryTitle(),
                                createJournalMessageDTO.getJournalEntryContent());

                Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId,
                                createJournalMessageDTO.getMessage());
                logService.createLog(loggedInPatient.getId(), LogTypes.JOURNAL_CONVERSATION_MESSAGE_CREATION, conversationId, "");
                return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
        }

        @GetMapping("/patients/journal-entry-conversation/{conversationId}/messages")
        @ResponseStatus(HttpStatus.OK)
        public JournalConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                Conversation completeConversation = conversationService.getAllMessagesFromConversation(
                                conversationId,
                                loggedInPatient);
                JournalEntryConversation journalConversation = (JournalEntryConversation) completeConversation;

                JournalConversationOutputDTO completeJournalConversationOutputDTO = ConversationMapper.INSTANCE
                                .convertEntityToJournalConversationOutputDTO(journalConversation);
                completeJournalConversationOutputDTO.setMessages(new ArrayList<>());

                for (Message message : completeConversation.getMessages()) {
                        message.setResponse(CryptographyUtil.decrypt(message.getResponse(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        message.setRequest(CryptographyUtil.decrypt(message.getRequest(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        completeJournalConversationOutputDTO.getMessages()
                                        .add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
                }
                return completeJournalConversationOutputDTO;
        }

        @DeleteMapping("/patients/journal-entry-conversation/{conversationId}")
        @ResponseStatus(HttpStatus.OK)
        public void deleteJournalChat(HttpServletRequest httpServletRequest,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                conversationService.deleteAllMessagesFromConversation(conversationId, loggedInPatient);
        }

}
