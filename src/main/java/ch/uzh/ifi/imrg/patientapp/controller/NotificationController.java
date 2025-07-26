package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutNotificationInputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    private final PatientService patientService;
    private final PatientRepository patientRepository;

    public NotificationController(PatientService patientService, PatientRepository patientRepository) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
    }

    @PutMapping("/patients/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public void createConversation(HttpServletRequest httpServletRequest, @RequestBody PutNotificationInputDTO putNotificationInputDTO) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        loggedInPatient.setGetNotifications(putNotificationInputDTO.isGetNotifications());
        patientRepository.save(loggedInPatient);
    }
}
