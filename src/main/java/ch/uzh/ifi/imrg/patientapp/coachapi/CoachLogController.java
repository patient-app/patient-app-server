package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.LogOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CoachLogController {
    private final LogService logService;

    public CoachLogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/coach/patients/{patientId}/logs/{logType}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<LogOutputDTO> listAll(@PathVariable String patientId, @PathVariable String logType) {
        return logService.getLogsByPatientIdAndLogType(patientId, logType);
    }

}
