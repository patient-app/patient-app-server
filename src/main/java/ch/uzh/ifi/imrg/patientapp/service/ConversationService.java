package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutConversationNameDTO;
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
    private final PromptBuilderService promptBuilderService;
    private final ChatbotTemplateRepository chatbotTemplateRepository;
    private final LogService logService;

    public ConversationService(ConversationRepository conversationRepository,
                               AuthorizationService authorizationService,
                               PromptBuilderService promptBuilderService, ChatbotTemplateRepository chatbotTemplateRepository,
                               MessageRepository messageRepository, LogService logService) {
        this.conversationRepository = conversationRepository;
        this.authorizationService = authorizationService;
        this.promptBuilderService = promptBuilderService;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
        this.logService = logService;
    }

    public GeneralConversation createConversation(Patient patient) {
        GeneralConversation conversation = new GeneralConversation();
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

    public void setConversationName(PutConversationNameDTO putConversationNameDTO, String conversationId,
            Patient loggedInPatient) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = (GeneralConversation) optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient,
                "You can't set the name of a chat of a different user.");
        ConversationMapper.INSTANCE.updateConversationFromPutConversationNameDTO(putConversationNameDTO, conversation);
        conversationRepository.save(conversation);
        logService.createLog(loggedInPatient.getId(), LogTypes.GENERAL_CONVERSATION_NAME_UPDATE, conversation.getId(),"");
    }

    public Conversation getAllMessagesFromConversation(String conversationId, Patient patient) {
        Conversation conversation = this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("No conversation found with this ID: " + conversationId));

        authorizationService.checkConversationAccess(conversation, patient,
                "You can't retrieve the messages of another user.");

        return conversation;
    }

    public List<GeneralConversation> getAllConversationsFromPatient(Patient patient) {
        return this.conversationRepository.getConversationByPatientId(patient.getId());
    }

    public void deleteAllMessagesFromConversation(String conversationId, Patient loggedInPatient) {

        Optional<Conversation> optionalConversation = this.conversationRepository
                .findById(conversationId);
        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            authorizationService.checkConversationAccess(conversation, loggedInPatient,
                    "You can't delete chats of a different user.");
            conversation.getMessages().clear();
            this.conversationRepository.save(conversation);
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
    }

    public void updateJournalConversationSystemPrompt(Patient patient, String journalConversationId,
            String journalEntryTitle,
            String journalEntryContent) {
        Conversation conversation = this.conversationRepository.findById(journalConversationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No conversation found with this ID: " + journalConversationId));

        ChatbotTemplate chatbotTemplate = chatbotTemplateRepository.findByPatientId(patient.getId()).getFirst();

        conversation.setSystemPrompt(promptBuilderService.getJournalSystemPrompt(chatbotTemplate, journalEntryTitle,
                journalEntryContent));

        this.conversationRepository.save(conversation);
    }

}
