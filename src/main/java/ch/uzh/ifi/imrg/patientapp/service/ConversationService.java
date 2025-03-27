package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation(Patient patient) {
        Conversation conversation = new Conversation();
        conversation.setPatient(patient);
        return this.conversationRepository.save(conversation);
    }
    public Conversation getAllMessagesFromConversation(String externalConversationId) {
        return this.conversationRepository.getConversationByExternalId(externalConversationId);
    }
}
