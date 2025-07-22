package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestAssignmentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CoachPsychologicalTestController {
    private final PsychologicalTestService psychologicalTestService;

    public CoachPsychologicalTestController(PsychologicalTestService psychologicalTestService) {
        this.psychologicalTestService = psychologicalTestService;
    }

    @GetMapping("/coach/patients/{patientId}/psychological-tests")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<PsychologicalTestNameOutputDTO> getPsychologicalTestNames(@PathVariable String patientId) {
        return psychologicalTestService.getAllTestNamesForPatientForCoach(patientId);
    }

    @GetMapping("/coach/patients/{patientId}/psychological-tests/available-tests")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<PsychologicalTestNameOutputDTO> getAvailablePsychologicalTestNames(@PathVariable String patientId) {
        return psychologicalTestService.getAllAvailableTestNamesForPatientForCoach(patientId);
    }

    @GetMapping("/coach/patients/{patientId}/psychological-tests/{psychologicalTestName}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<PsychologicalTestOutputDTO> getPsychologicalTestResults(@PathVariable String patientId,
                                                                        @PathVariable String psychologicalTestName) {
        return psychologicalTestService.getPsychologicalTestResultsForCoach(patientId, psychologicalTestName);
    }

    @PostMapping("/coach/patients/{patientId}/psychological-tests/{psychologicalTestName}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public void createPsychologicalTest(@PathVariable String patientId,
                                        @PathVariable String psychologicalTestName,
                                        @RequestBody PsychologicalTestAssignmentInputDTO psychologicalTestAssignmentInputDTO) {
        psychologicalTestService.createPsychologicalTestAssginment(patientId, psychologicalTestName, psychologicalTestAssignmentInputDTO);
    }

    @PutMapping("/coach/patients/{patientId}/psychological-tests/{psychologicalTestName}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public void updatePsychologicalTest(@PathVariable String patientId,
                                        @PathVariable String psychologicalTestName,
                                        @RequestBody PsychologicalTestAssignmentInputDTO psychologicalTestAssignmentInputDTO) {
        psychologicalTestService.updatePsychologicalTestAssginment(patientId, psychologicalTestName, psychologicalTestAssignmentInputDTO);
    }
}
