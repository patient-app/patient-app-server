package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;

public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        return this.conversationRepository.save(conversation);
    }
    public Conversation getAllMessagesFromConversation(String externalConversationId) {
        return this.conversationRepository.getConversationByExternalId(externalConversationId);
    }
}
