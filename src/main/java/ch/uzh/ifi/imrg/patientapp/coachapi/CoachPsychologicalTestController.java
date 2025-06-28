package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public List<PsychologicalTestNameOutputDTO> getPsychologicalTestResults(@PathVariable String patientId) {
        return psychologicalTestService.getAllTestNamesForPatient(patientId);
    }

    @GetMapping("/coach/patients/{patientId}/psychological-tests/{psychologicalTestName}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<PsychologicalTestOutputDTO> getPsychologicalTestResults(@PathVariable String patientId,
                                                          @PathVariable String exerciseId) {
        return psychologicalTestService.getPsyhologicalTestResults(patientId, exerciseId);
    }
}
