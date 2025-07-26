package ch.uzh.ifi.imrg.patientapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PushSubscriptionDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PushNotificationService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
public class NotificationSubscriptionController {

    private final PushNotificationService pushNotificationService;
    private final PatientService patientService;

    public NotificationSubscriptionController(PushNotificationService pushNotificationService,
            PatientService patientService) {
        this.pushNotificationService = pushNotificationService;
        this.patientService = patientService;
    }

    @PostMapping("/patients/notification/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(HttpServletRequest request, @RequestBody PushSubscriptionDTO dto) {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(request);

        pushNotificationService.subscribe(dto, loggedInPatient);
    }

    // TODO: delete, this is only for TESTING!!!!!!!
    @PostMapping("/patients/{patientId}/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTest(@PathVariable("patientId") String patientId, @RequestParam("message") String message) {

        pushNotificationService.sendToPatient(patientId, "Test Notification", message);

    }
}
