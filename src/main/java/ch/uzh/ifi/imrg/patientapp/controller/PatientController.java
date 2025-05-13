package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.TermsOutputDTO;
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

    @PutMapping("/patients/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePatientSettings(){
        //TODO: implement language settings (update)
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

    @GetMapping("/patients/terms")
    @ResponseStatus(HttpStatus.OK)
    public TermsOutputDTO getTerms() {
        return new TermsOutputDTO("""
                        <p><strong>Last updated:</strong> May 12, 2025</p>
                        <br>
                        <h2>1. Acceptance of Terms</h2>
                        <p>
                          By accessing and using this application, you agree to be bound by these Terms and Conditions.
                          If you do not agree, you may not use the app.
                        </p>
                        <br>
                        <h2>2. Use of the App</h2>
                        <p>
                          You agree to use the app only for lawful purposes and in a way that does not infringe on the rights of others.
                        </p>
                        <br>
                        <h2>3. Intellectual Property</h2>
                        <p>
                          All content, trademarks, and data on this app are the property of the app provider and protected by law.
                        </p>
                        <br>
                        <h2>4. Disclaimer</h2>
                        <p>
                          The app is provided "as is" without warranties of any kind. We do not guarantee the accuracy, reliability, or availability of the content.
                        </p>
                        <br>
                        <h2>5. Changes to Terms</h2>
                        <p>
                          We reserve the right to update these terms at any time. Continued use of the app constitutes acceptance of the new terms.
                        </p>
                        <br>
                        <h2>6. Contact</h2>
                        <p>
                          If you have any questions about these terms, please contact us at <a href="mailto:support@example.com">support@example.com</a>.
                        </p>
                        """
        );
    }



}
