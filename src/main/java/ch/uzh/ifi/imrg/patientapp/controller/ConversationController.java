package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PatientMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ConversationController {
    @PostMapping("/patients/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientOutputDTO createConversation(
            @RequestBody CreatePatientDTO patientInputDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Patient patient = PatientMapper.INSTANCE.convertCreatePatientDtoToEntity(patientInputDTO);
        Conversation conversationCreated = new Conversation();
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(createdPatient);
    }
}
