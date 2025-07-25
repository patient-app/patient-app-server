package ch.uzh.ifi.imrg.patientapp.coachapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateCoachEmailDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PatientMapper;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.utils.JwtUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
public class CoachPatientController {

    private final PatientService patientService;

    CoachPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/coach/patients/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public PatientOutputDTO registerPatient(
            @Valid @RequestBody CreatePatientDTO patientInputDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {

        String rawKey = httpServletRequest.getHeader("X-Coach-Key");

        if (!patientInputDTO.getCoachAccessKey().equals(rawKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "coachAccessKey in request body does not match X-Coach-Key in header");
        }

        Patient patient = PatientMapper.INSTANCE.convertCreatePatientDTOToEntity(patientInputDTO);
        Patient createdPatient = patientService.registerPatient(patient, httpServletRequest, httpServletResponse);

        // TODO remove this whe refractoring registration:
        JwtUtil.removeJwtCookie(httpServletResponse);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(createdPatient);
    }

    @DeleteMapping("/coach/patients/{patientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deletePatient(@PathVariable String patientId) {
        patientService.removePatient(patientId);
    }

    @PutMapping("/coach/patients/{patientId}/coach-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void setCoachEmail(@RequestBody UpdateCoachEmailDTO dto, @PathVariable String patientId) {
        patientService.updateCoachEmail(patientId, dto.getCoachEmail());
    }
}
