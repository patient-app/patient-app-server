package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseConversationRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateConversationDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final AuthorizationService authorizationService;
    private final ExerciseConversationRepository exerciseConversationRepository;
    private final PromptBuilderService promptBuilderService;
    private final ChatbotTemplateRepository chatbotTemplateRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               AuthorizationService authorizationService, ExerciseConversationRepository exerciseConversationRepository, PromptBuilderService promptBuilderService, ChatbotTemplateRepository chatbotTemplateRepository) {
        this.conversationRepository = conversationRepository;
        this.authorizationService = authorizationService;
        this.exerciseConversationRepository = exerciseConversationRepository;
        this.promptBuilderService = promptBuilderService;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
    }

    public GeneralConversation createConversation(Patient patient, CreateConversationDTO createConversationDTO) {
        GeneralConversation conversation = ConversationMapper.INSTANCE.createConversationDTOToConversation(createConversationDTO);
        ChatbotTemplate chatbotTemplate = chatbotTemplateRepository.findByPatientId(patient.getId()).getFirst();
        conversation.setSystemPrompt(promptBuilderService.getSystemPrompt(chatbotTemplate));
        conversation.setWelcomeMessage(chatbotTemplate.getWelcomeMessage());
        conversation.setPatient(patient);
        return this.conversationRepository.save(conversation);
    }

    public void deleteConversation(String conversationId, Patient loggedInPatient) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = (GeneralConversation) optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient,
                "You can't delete chats of a different user.");
        conversationRepository.delete(conversation);
    }

    public void updateSharing(PutSharingDTO putSharingDTO, String conversationId, Patient loggedInPatient) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = (GeneralConversation) optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient,
                "You can't set access rights for chats of a different user.");
        ConversationMapper.INSTANCE.updateConversationFromPutSharingDTO(putSharingDTO, conversation);
        conversationRepository.save(conversation);
    }

    public Conversation getAllMessagesFromConversation(String conversationId, Patient patient) {
        Conversation conversation = this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("No conversation found with this ID: " + conversationId));

        authorizationService.checkConversationAccess(conversation, patient,
                "You can't retrieve the messages of another user.");

        return conversation;
    }


    public List<GeneralConversation> getAllConversationsFromPatient(Patient patient) {
        return  this.conversationRepository.getConversationByPatientId(patient.getId());
    }

    public void deleteAllMessagesFromExerciseConversation(String conversationId, Patient loggedInPatient) {

        Optional<ExerciseConversation> optionalExerciseConversation = this.exerciseConversationRepository.findById(conversationId);
        if (optionalExerciseConversation.isPresent()) {
            ExerciseConversation exerciseConversation = optionalExerciseConversation.get();
            authorizationService.checkConversationAccess(exerciseConversation, loggedInPatient,
                    "You can't delete chats of a different user.");
            exerciseConversation.getMessages().clear();
            this.exerciseConversationRepository.save(exerciseConversation);
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
    }

}
