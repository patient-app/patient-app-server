package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CreateConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PsychologicalTestController {
    private final PatientService patientService;
    private final PsychologicalTestService psychologicalTestService;

    public PsychologicalTestController(PatientService patientService, PsychologicalTestService psychologicalTestService) {
        this.patientService = patientService;
        this.psychologicalTestService = psychologicalTestService;
    }

    @PostMapping("/patients/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPsychologicalTest(HttpServletRequest httpServletRequest, @RequestBody PsychologicalTestInputDTO psychologicalTestInputDTO) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        psychologicalTestService.createPsychologicalTest(loggedInPatient, psychologicalTestInputDTO);
    }
}
