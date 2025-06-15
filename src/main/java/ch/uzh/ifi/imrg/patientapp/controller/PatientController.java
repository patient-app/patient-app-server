package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutLanguageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutOnboardedDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PatientMapper;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

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

    @PutMapping("patients/language")
    @ResponseStatus(HttpStatus.OK)
    public void setLanguage(@RequestBody PutLanguageDTO putLanguageDTO, HttpServletRequest httpServletRequest) throws IOException {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        loggedInPatient.setLanguage(putLanguageDTO.getLanguage());
        patientService.setField(loggedInPatient);
    }

    @GetMapping("patients/language")
    @ResponseStatus(HttpStatus.OK)
    public PatientOutputDTO getLanguage(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(loggedInPatient);
    }

    @PutMapping("patients/onboarded")
    @ResponseStatus(HttpStatus.OK)
    public void setOnboarded(@RequestBody PutOnboardedDTO putOnboardedDTO, HttpServletRequest httpServletRequest) throws IOException {

        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        if(!loggedInPatient.isOnboarded()){
            loggedInPatient.setOnboarded(putOnboardedDTO.isOnboarded());
            patientService.setField(loggedInPatient);
        }

    }
    @GetMapping("patients/onboarded")
    @ResponseStatus(HttpStatus.OK)
    public PatientOutputDTO getOnboarded(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return PatientMapper.INSTANCE.convertEntityToPatientOutputDTO(loggedInPatient);
    }
}
