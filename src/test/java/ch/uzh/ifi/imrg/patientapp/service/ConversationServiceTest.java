package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.parameters.P;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ConversationServiceTest {
    private ConversationRepository conversationRepository;
    private ConversationService conversationService;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setup() {
        conversationRepository = mock(ConversationRepository.class);
        conversationService = new ConversationService(conversationRepository, authorizationService);
    }

    @Test
    void createConversation_shouldSetPatientAndSave() {
        Patient patient = new Patient();
        Conversation savedConversation = new Conversation();
        savedConversation.setPatient(patient);

        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);

        Conversation result = conversationService.createConversation(patient);

        assertEquals(patient, result.getPatient());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }
/*
    @Test
    void getAllMessagesFromConversation_shouldReturnConversationIfExists() {
        String externalId = "conv-123";
        Conversation conversation = new Conversation();
        Patient patient = new Patient();
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getAllMessagesFromConversation(externalId,patient);

        assertEquals(conversation, result);
        verify(conversationRepository).getConversationByExternalId(externalId);
    }*/

    @Test
    void getAllMessagesFromConversation_shouldThrowIfNotFound() {
        String externalId = "nonexistent-id";
        Patient patient = new Patient();
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> conversationService.getAllMessagesFromConversation(externalId, patient)
        );

        assertTrue(exception.getMessage().contains("No conversation found with external ID"));
    }
}
