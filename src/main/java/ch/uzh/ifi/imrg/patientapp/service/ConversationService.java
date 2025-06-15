package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
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

    public ConversationService(ConversationRepository conversationRepository, AuthorizationService authorizationService) {
        this.conversationRepository = conversationRepository;
        this.authorizationService = authorizationService;
    }

    public Conversation createConversation(Patient patient) {
        Conversation conversation = new Conversation();
        conversation.setPatient(patient);
        return this.conversationRepository.save(conversation);
    }

    public void updateSharing(PutSharingDTO putSharingDTO, String externalConversationId, Patient loggedInPatient) {
        Optional<Conversation> optionalConversation = conversationRepository.getConversationByExternalId(externalConversationId);
        Conversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = optionalConversation.get();
        }else {
            throw new NoSuchElementException("No conversation found with external ID: " + externalConversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient, "You can't set access rights for chats of a different user.");
        ConversationMapper.INSTANCE.updateConversationFromPutSharingDTO(putSharingDTO, conversation);
        conversationRepository.save(conversation);
    }

    public Conversation getAllMessagesFromConversation(String externalConversationId, Patient patient) {
        Optional<Conversation> optionalConversation = this.conversationRepository.getConversationByExternalId(externalConversationId);

        if (optionalConversation.isPresent()) {
            authorizationService.checkConversationAccess(optionalConversation.get(), patient, "You can't retrieve the messages of an other user.");
            return optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + externalConversationId);
        }
    }
    public List<Conversation> getAllConversationsFromPatient(Patient patient){
        return this.conversationRepository.getConversationByPatientId(patient.getId());
    }

}
