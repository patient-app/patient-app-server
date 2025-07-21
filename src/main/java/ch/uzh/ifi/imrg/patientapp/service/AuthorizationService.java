package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public void checkConversationAccess(Conversation conversation, Patient patient, String errorMessage) {
        if (!conversation.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException(errorMessage);
        }
    }
    public void checkExerciseAccess(Exercise exercise, Patient patient, String errorMessage) {
        if (!exercise.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
