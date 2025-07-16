package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMessageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.CompleteExerciseConversationOutputDTO;
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

        public ExerciseConversationController(PatientService patientService, MessageService messageService,
                        ConversationService conversationService) {
                this.patientService = patientService;
                this.messageService = messageService;
                this.conversationService = conversationService;
        }

        @PostMapping("/patients/exercise-conversation/{conversationId}/messages")
        @ResponseStatus(HttpStatus.OK)
        public MessageOutputDTO sendMessage(HttpServletRequest httpServletRequest,
                        @RequestBody CreateMessageDTO createMessageDTO,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                Message answeredMessage = messageService.generateAnswer(loggedInPatient, conversationId,
                                createMessageDTO.getMessage());
                return MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(answeredMessage);
        }

        @GetMapping("/patients/exercise-conversation/{conversationId}/messages")
        @ResponseStatus(HttpStatus.OK)
        public CompleteExerciseConversationOutputDTO getAllMessages(HttpServletRequest httpServletRequest,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                Conversation completeConversation = conversationService.getAllMessagesFromConversation(
                                conversationId,
                                loggedInPatient);
                ExerciseConversation exerciseConversation = (ExerciseConversation) completeConversation;

                CompleteExerciseConversationOutputDTO completeExerciseConversationOutputDTO = ConversationMapper.INSTANCE
                                .convertEntityToCompleteExerciseConversationOutputDTO(exerciseConversation);
                completeExerciseConversationOutputDTO.setMessages(new ArrayList<>());

                for (Message message : completeConversation.getMessages()) {
                        message.setResponse(CryptographyUtil.decrypt(message.getResponse(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        message.setRequest(CryptographyUtil.decrypt(message.getRequest(),
                                        CryptographyUtil.decrypt(loggedInPatient.getPrivateKey())));
                        completeExerciseConversationOutputDTO.getMessages()
                                        .add(MessageMapper.INSTANCE.convertEntityToMessageOutputDTO(message));
                }
                return completeExerciseConversationOutputDTO;

        }

        @DeleteMapping("/patients/exercise-conversation/{conversationId}")
        @ResponseStatus(HttpStatus.OK)
        public void deleteExerciseChat(HttpServletRequest httpServletRequest,
                        @PathVariable String conversationId) {
                Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
                conversationService.deleteAllMessagesFromConversation(conversationId, loggedInPatient);
        }
}
