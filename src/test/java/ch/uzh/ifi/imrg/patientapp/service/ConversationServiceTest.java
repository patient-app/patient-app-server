package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @InjectMocks
    private ConversationService conversationService;
    @Mock
    private AuthorizationService authorizationService;


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

    @Test
    void getAllMessagesFromConversation_shouldReturnConversationIfExists() {
        String externalId = "conv-123";
        Conversation conversation = new Conversation();
        Patient patient = new Patient();
        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getAllMessagesFromConversation(externalId,patient);

        assertEquals(conversation, result);
        verify(conversationRepository).getConversationByExternalId(externalId);
    }

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

    @Test
    void testDeleteConversation_ValidConversation_DeletesSuccessfully() {
        // Arrange
        String externalId = "conv123";
        Patient patient = new Patient();
        Conversation conversation = new Conversation();

        when(conversationRepository.getConversationByExternalId(externalId))
                .thenReturn(Optional.of(conversation));

        // Act
        conversationService.deleteConversation(externalId, patient);

        // Assert
        verify(conversationRepository).getConversationByExternalId(externalId);
        verify(conversationRepository).delete(conversation);
    }

    @Test
    void testDeleteConversation_ConversationNotFound_ThrowsException() {
        // Arrange
        String externalId = "nonexistent";
        Patient patient = new Patient();

        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                conversationService.deleteConversation(externalId, patient)
        );

        assertEquals("No conversation found with external ID: " + externalId, exception.getMessage());
        verify(conversationRepository).getConversationByExternalId(externalId);
        verifyNoInteractions(authorizationService);
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void testUpdateSharing_ValidConversation_UpdatesAndSaves() {
        // Arrange
        String externalId = "conv123";
        Patient patient = new Patient();
        PutSharingDTO dto = new PutSharingDTO();

        Conversation conversation = new Conversation();

        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.of(conversation));

        // Act
        conversationService.updateSharing(dto, externalId, patient);

        // Assert
        verify(conversationRepository).getConversationByExternalId(externalId);
        verify(authorizationService).checkConversationAccess(conversation, patient,
                "You can't set access rights for chats of a different user.");
        verify(conversationRepository).save(conversation);
    }
    @Test
    void testUpdateSharing_ConversationNotFound_ThrowsException() {
        // Arrange
        String externalId = "not-found";
        Patient patient = new Patient();
        PutSharingDTO dto = new PutSharingDTO();

        when(conversationRepository.getConversationByExternalId(externalId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                conversationService.updateSharing(dto, externalId, patient)
        );

        assertEquals("No conversation found with external ID: " + externalId, exception.getMessage());
        verify(conversationRepository).getConversationByExternalId(externalId);
        verifyNoInteractions(authorizationService);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void testGetAllConversationsFromPatient_ReturnsConversations() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p123");

        Conversation conv1 = new Conversation();
        Conversation conv2 = new Conversation();
        List<Conversation> expectedConversations = List.of(conv1, conv2);

        when(conversationRepository.getConversationByPatientId("p123"))
                .thenReturn(expectedConversations);

        // Act
        List<Conversation> result = conversationService.getAllConversationsFromPatient(patient);

        // Assert
        assertEquals(expectedConversations, result);
        verify(conversationRepository).getConversationByPatientId("p123");
    }


}
