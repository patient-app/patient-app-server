package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ConversationMapper;
import ch.uzh.ifi.imrg.patientapp.service.ConversationService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil.decrypt;

public class ConversationController {
    private final PatientService patientService;
    private final ConversationService conversationService;

    ConversationController(PatientService patientService,
                           ConversationService conversationService) {
        this.patientService = patientService;
        this.conversationService = conversationService;
    }

    @PostMapping("/patients/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateConversationOutputDTO createConversation(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Conversation createdConversation = conversationService.createConversation();
        // add the conversation to the patient
        patientService.addConversationToPatient(loggedInPatient, createdConversation);
        String encryptedConversationId = CryptographyUtil.encrypt(createdConversation.getId(), decrypt(loggedInPatient.getPrivateKey()));
        return new CreateConversationOutputDTO(encryptedConversationId);
    }
}
