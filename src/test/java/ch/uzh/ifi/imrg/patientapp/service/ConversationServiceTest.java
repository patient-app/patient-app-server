package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseConversationRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ExerciseConversationRepository exerciseConversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void createConversation_shouldSetPatientAndSave() {
        Patient patient = new Patient();
        GeneralConversation savedConversation = new GeneralConversation();
        savedConversation.setPatient(patient);

        when(conversationRepository.save(any(GeneralConversation.class))).thenReturn(savedConversation);

        GeneralConversation result = conversationService.createConversation(patient);

        assertEquals(patient, result.getPatient());
        verify(conversationRepository, times(1)).save(any(GeneralConversation.class));
    }

    @Test
    void getAllMessagesFromConversation_shouldReturnConversationIfExists() {
        String externalId = "conv-123";
        GeneralConversation conversation = new GeneralConversation();
        Patient patient = new Patient();
        when(conversationRepository.findById(externalId)).thenReturn(Optional.of(conversation));

        GeneralConversation result = conversationService.getAllMessagesFromConversation(externalId, patient);

        assertEquals(conversation, result);
        verify(conversationRepository).findById(externalId);
    }

    @Test
    void getAllMessagesFromConversation_shouldThrowIfNotFound() {
        String externalId = "nonexistent-id";
        Patient patient = new Patient();
        when(conversationRepository.findById(externalId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> conversationService.getAllMessagesFromConversation(externalId, patient));

        assertTrue(exception.getMessage().contains("No conversation found with external ID"));
    }

    @Test
    void testDeleteConversation_ValidConversation_DeletesSuccessfully() {
        // Arrange
        String externalId = "conv123";
        Patient patient = new Patient();
        GeneralConversation conversation = new GeneralConversation();

        when(conversationRepository.findById(externalId))
                .thenReturn(Optional.of(conversation));

        // Act
        conversationService.deleteConversation(externalId, patient);

        // Assert
        verify(conversationRepository).findById(externalId);
        verify(conversationRepository).delete(conversation);
    }

    @Test
    void testDeleteConversation_ConversationNotFound_ThrowsException() {
        // Arrange
        String externalId = "nonexistent";
        Patient patient = new Patient();

        when(conversationRepository.findById(externalId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> conversationService.deleteConversation(externalId, patient));

        assertEquals("No conversation found with external ID: " + externalId, exception.getMessage());
        verify(conversationRepository).findById(externalId);
        verifyNoInteractions(authorizationService);
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void testUpdateSharing_ValidConversation_UpdatesAndSaves() {
        // Arrange
        String externalId = "conv123";
        Patient patient = new Patient();
        PutSharingDTO dto = new PutSharingDTO();

        GeneralConversation conversation = new GeneralConversation();

        when(conversationRepository.findById(externalId)).thenReturn(Optional.of(conversation));

        // Act
        conversationService.updateSharing(dto, externalId, patient);

        // Assert
        verify(conversationRepository).findById(externalId);
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

        when(conversationRepository.findById(externalId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> conversationService.updateSharing(dto, externalId, patient));

        assertEquals("No conversation found with external ID: " + externalId, exception.getMessage());
        verify(conversationRepository).findById(externalId);
        verifyNoInteractions(authorizationService);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void testGetAllConversationsFromPatient_ReturnsConversations() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p123");

        GeneralConversation conv1 = new GeneralConversation();
        GeneralConversation conv2 = new GeneralConversation();
        List<GeneralConversation> expectedConversations = List.of(conv1, conv2);

        when(conversationRepository.getConversationByPatientId("p123"))
                .thenReturn(expectedConversations);

        // Act
        List<GeneralConversation> result = conversationService.getAllConversationsFromPatient(patient);

        // Assert
        assertEquals(expectedConversations, result);
        verify(conversationRepository).getConversationByPatientId("p123");
    }

    @Test
    void testDeleteAllMessagesFromExerciseConversation_ValidConversation_ClearsMessagesAndSaves() {
        // Arrange
        String conversationId = "conv123";
        Patient patient = new Patient();

        ExerciseConversation exerciseConversation = new ExerciseConversation();
        exerciseConversation.setMessages(new ArrayList<>(List.of(new Message(), new Message())));

        when(exerciseConversationRepository.findById(conversationId)).thenReturn(Optional.of(exerciseConversation));
        when(exerciseConversationRepository.findById(conversationId)).thenReturn(Optional.of(exerciseConversation));

        // Act
        conversationService.deleteAllMessagesFromExerciseConversation(conversationId, patient);

        // Assert
        verify(exerciseConversationRepository).findById(conversationId);
        verify(authorizationService).checkConversationAccess(eq(exerciseConversation), eq(patient),
                contains("You can't delete chats of a different user."));
        assertTrue(exerciseConversation.getMessages().isEmpty(), "Messages should be cleared");
        verify(exerciseConversationRepository).save(exerciseConversation);
    }

    @Test
    void testDeleteAllMessagesFromExerciseConversation_ConversationNotFound_ThrowsException() {
        // Arrange
        String conversationId = "nonexistent";
        Patient patient = new Patient();

        when(exerciseConversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> conversationService.deleteAllMessagesFromExerciseConversation(conversationId, patient));

        assertEquals("No conversation found with external ID: " + conversationId, exception.getMessage());
        verify(exerciseConversationRepository).findById(conversationId);
        verifyNoInteractions(authorizationService);
        verify(exerciseConversationRepository, never()).save(any());
    }



}
