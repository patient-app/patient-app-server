package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }

    @Test
    void checkConversationAccess_shouldPass_whenPatientMatches() {
        Patient patient = new Patient();
        patient.setId("p123");

        Conversation conversation = new GeneralConversation();
        conversation.setPatient(patient);

        assertDoesNotThrow(() ->
                authorizationService.checkConversationAccess(conversation, patient, "Access denied"));
    }

    @Test
    void checkConversationAccess_shouldThrow_whenPatientDoesNotMatch() {
        Patient patient = new Patient();
        patient.setId("p123");

        Patient otherPatient = new Patient();
        otherPatient.setId("p456");

        Conversation conversation = new GeneralConversation();
        conversation.setPatient(otherPatient);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkConversationAccess(conversation, patient, "Access denied"));

        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void checkExerciseAccess_shouldPass_whenPatientMatches() {
        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise = new Exercise();
        exercise.setPatient(patient);

        assertDoesNotThrow(() ->
                authorizationService.checkExerciseAccess(exercise, patient, "Access denied"));
    }

    @Test
    void checkExerciseAccess_shouldThrow_whenPatientDoesNotMatch() {
        Patient patient = new Patient();
        patient.setId("p123");

        Patient otherPatient = new Patient();
        otherPatient.setId("p456");

        Exercise exercise = new Exercise();
        exercise.setPatient(otherPatient);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkExerciseAccess(exercise, patient, "Access denied"));

        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void checkPsychologicalTestAssignmentAccess_shouldThrow_whenPatientDoesNotMatch() {
        Patient patient = new Patient();
        patient.setId("p123");

        Patient otherPatient = new Patient();
        otherPatient.setId("p456");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(otherPatient);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkPsychologicalTestAssignmentAccess(assignment, patient, "Access denied"));

        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void checkPsychologicalTestAssignmentAccess_shouldPass_whenPatientMatches() {
        Patient patient = new Patient();
        patient.setId("p123");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);

        assertDoesNotThrow(() ->
                authorizationService.checkPsychologicalTestAssignmentAccess(assignment, patient, "Access denied"));
    }

}
