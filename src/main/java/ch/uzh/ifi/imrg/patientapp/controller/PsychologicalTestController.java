package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameAndPatientIdOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/patients/{patientId}/psychological-tests")
    @ResponseStatus(HttpStatus.OK)
    public List<PsychologicalTestNameAndPatientIdOutputDTO> getPsychologicalTestNames(HttpServletRequest httpServletRequest, @PathVariable String patientId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return psychologicalTestService.getAllTestNamesForPatient(loggedInPatient,patientId);
    }

    @GetMapping("/patients/{patientId}/psychological-tests/{psychologicalTestName}")
    @ResponseStatus(HttpStatus.OK)
    public List<PsychologicalTestOutputDTO> getPsychologicalTestResults(HttpServletRequest httpServletRequest,
                                                                        @PathVariable String patientId,
                                                                        @PathVariable String psychologicalTestName) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return psychologicalTestService.getPsychologicalTestResults(loggedInPatient,patientId, psychologicalTestName);
    }
}
