package ch.uzh.ifi.imrg.patientapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PatientMapper;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class PatientController {

    private final PatientService patientService;

    PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients/me")
    @ResponseStatus(HttpStatus.OK)
    public PatientOutputDTO getCurrentlyLoggedInPatient(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(loggedInPatient);
    }

    @PostMapping("/patients/register")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientOutputDTO registerPatient(
            @RequestBody CreatePatientDTO patientInputDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Patient patient = PatientMapper.INSTANCE.convertCreatePatientDTOToEntity(patientInputDTO);
        Patient createdPatient = patientService.registerPatient(patient, httpServletRequest, httpServletResponse);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(createdPatient);
    }

    @PostMapping("/patients/login")
    @ResponseStatus(HttpStatus.OK)
    public PatientOutputDTO loginTherapist(
            @RequestBody LoginPatientDTO loginPatientDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Patient loggedInPatient = patientService.loginPatient(loginPatientDTO, httpServletRequest,
                httpServletResponse);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(loggedInPatient);
    }

    @PostMapping("/patients/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logoutTherapist(HttpServletResponse httpServletResponse) {
        patientService.logoutPatient(httpServletResponse);
    }

}
