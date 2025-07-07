package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseConversationRepository;
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
    private final ExerciseConversationRepository exerciseConversationRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               AuthorizationService authorizationService, ExerciseConversationRepository exerciseConversationRepository) {
        this.conversationRepository = conversationRepository;
        this.authorizationService = authorizationService;
        this.exerciseConversationRepository = exerciseConversationRepository;
    }

    public GeneralConversation createConversation(Patient patient) {
        GeneralConversation conversation = new GeneralConversation();
        conversation.setPatient(patient);
        return this.conversationRepository.save(conversation);
    }

    public void deleteConversation(String conversationId, Patient loggedInPatient) {
        Optional<GeneralConversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient,
                "You can't delete chats of a different user.");
        conversationRepository.delete(conversation);
    }

    public void updateSharing(PutSharingDTO putSharingDTO, String conversationId, Patient loggedInPatient) {
        Optional<GeneralConversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation;
        if (optionalConversation.isPresent()) {
            conversation = optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
        authorizationService.checkConversationAccess(conversation, loggedInPatient,
                "You can't set access rights for chats of a different user.");
        ConversationMapper.INSTANCE.updateConversationFromPutSharingDTO(putSharingDTO, conversation);
        conversationRepository.save(conversation);
    }

    public GeneralConversation getAllMessagesFromConversation(String conversationId, Patient patient) {
        Optional<GeneralConversation> optionalConversation = this.conversationRepository.findById(conversationId);

        if (optionalConversation.isPresent()) {
            authorizationService.checkConversationAccess(optionalConversation.get(), patient,
                    "You can't retrieve the messages of an other user.");
            return optionalConversation.get();
        } else {
            throw new NoSuchElementException("No conversation found with external ID: " + conversationId);
        }
    }

    public List<GeneralConversation> getAllConversationsFromPatient(Patient patient) {
        return this.conversationRepository.getConversationByPatientId(patient.getId());
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
