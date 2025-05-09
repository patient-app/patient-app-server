package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.utils.JwtUtil;
import ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil.decrypt;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(
            @Qualifier("patientRepository") PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient getCurrentlyLoggedInPatient(HttpServletRequest httpServletRequest) {
        String email = JwtUtil.validateJWTAndExtractEmail(httpServletRequest);
        Patient foundPatient = patientRepository.getPatientByEmail((email));
        if (foundPatient != null) {
            return foundPatient;
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Client could not be found. Please check, username and password");
    }

    public Patient addConversationToPatient(Patient patient, Conversation conversation) {
        patient.getConversations().add(conversation);
        patientRepository.save(patient);
        return patient;
    }
    // Registering patients is not necessary in the patient app (this is done via
    // the therapist app) but for testing purposes it might be usefule
    public Patient registerPatient(
            Patient patient,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        if (patient.getEmail() == null) {
            throw new Error("Creating client failed because no email was specified");
        }
        if (patient.getPassword() == null) {
            throw new Error("Creating client failed because no password was specified");
        }
        if (patient.getId() != null && this.patientRepository.existsById(patient.getId())) {
            throw new Error(
                    "Creating client failed because patient with this ID already exists");
        }
        if (patient.getEmail() != null && patientRepository.existsByEmail(patient.getEmail())) {
            throw new Error(
                    "Creating client failed because patient with this email" + patient.getEmail() +"already exists");
        }

        patient.setPassword(PasswordUtil.encryptPassword(patient.getPassword()));
        patient.setPrivateKey(CryptographyUtil.encrypt(CryptographyUtil.generatePrivateKey()));

        Patient createdPatient = this.patientRepository.save(patient);
        String jwt = JwtUtil.createJWT(patient.getEmail());
        JwtUtil.addJwtCookie(httpServletResponse, httpServletRequest, jwt);
        return createdPatient;
    }

    public Patient loginPatient(
            LoginPatientDTO loginPatientDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Patient foundPatient = patientRepository.getPatientByEmail(loginPatientDTO.getEmail());
        if (foundPatient == null) {
            throw new Error("No client with email: " + loginPatientDTO.getEmail() + " exists");
        }

        if (!PasswordUtil.checkPassword(
                loginPatientDTO.getPassword(), foundPatient.getPassword())) {
            throw new Error("Wrong password.");
        }

        String jwt = JwtUtil.createJWT(loginPatientDTO.getEmail());
        JwtUtil.addJwtCookie(httpServletResponse, httpServletRequest, jwt);
        return foundPatient;
    }

    public void logoutPatient(HttpServletResponse httpServletResponse) {
        JwtUtil.removeJwtCookie(httpServletResponse);
    }

}
