package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;


@Service
public class AuthorizationService {
    private final View error;

    public AuthorizationService(View error) {
        this.error = error;
    }

    public void checkConversationAccess (Conversation conversation, Patient patient, String errorMessage){
        if(!conversation.getPatient().getId().equals(patient.getId())){
            throw new AccessDeniedException(errorMessage);        }
    }
}
