package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.ChangePasswordDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.utils.JwtUtil;
import ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil.decrypt;

import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final DocumentService documentService;
    private final LogService logService;
    private final EmailService emailService;

    @Autowired
    public PatientService(
            @Qualifier("patientRepository") PatientRepository patientRepository,
            DocumentService documentService, LogService logService, EmailService emailService) {
        this.patientRepository = patientRepository;
        this.documentService = documentService;
        this.logService = logService;
        this.emailService = emailService;
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

    public Patient addConversationToPatient(Patient patient, GeneralConversation conversation) {
        patient.getConversations().add(conversation);
        patientRepository.save(patient);
        logService.createLog(patient.getId(), LogTypes.GENERAL_CONVERSATION_CREATION, conversation.getId(), "");
        return patient;
    }

    public void setField(Patient patient) {
        patientRepository.save(patient);
    }

    // Registering patients is not necessary in the patient app (this is done via
    // the therapist app) but for testing purposes it might be useful
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
                    "Creating client failed because patient with this email" + patient.getEmail() + "already exists");
        }

        patient.setPassword(PasswordUtil.encryptPassword(patient.getPassword()));
        patient.setPrivateKey(CryptographyUtil.encrypt(CryptographyUtil.generatePrivateKey()));

        patient.setCoachAccessKey(PasswordUtil.encryptPassword(patient.getCoachAccessKey()));
        if (patient.getId() == null) {
            patient.setId(UUID.randomUUID().toString());
        }
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

    public void changePassword(Patient loggedInPatient, ChangePasswordDTO changePasswordDTO) {

        // veify old password
        if (!PasswordUtil.checkPassword(changePasswordDTO.getCurrentPassword(), loggedInPatient.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Old password is incorrect");
        }

        // encode and save new password
        loggedInPatient.setPassword(PasswordUtil.encryptPassword(changePasswordDTO.getNewPassword()));
        patientRepository.save(loggedInPatient);

    }

    public void removePatient(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("Patient " + patientId + " not found");
        }

        documentService.removeAllDocumentsForPatient(patientId);

        patientRepository.deleteById(patientId);
    }

    public void resetPasswordAndNotify(String email) {

        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No patient found with email: " + email));

        PasswordUtil.Alphabet alphabet = "uk".equalsIgnoreCase(patient.getLanguage()) ? PasswordUtil.Alphabet.CYRILLIC
                : PasswordUtil.Alphabet.LATIN;

        String newPassword = PasswordUtil.generatePassword(alphabet);

        patient.setPassword(PasswordUtil.encryptPassword(newPassword));
        patientRepository.save(patient);

        String lang = patient.getLanguage() != null ? patient.getLanguage().toLowerCase() : "en";
        String subject;
        String body;

        if ("uk".equals(lang)) {
            subject = "Lumina — Ваш новий пароль";
            body = String.join("\n",
                    "Привіт, " + patient.getName() + "!",
                    "",
                    "Ваш пароль було скинуто за вашим запитом.",
                    "Ваш новий пароль:",
                    "",
                    "    " + newPassword,
                    "",
                    "Будь ласка, увійдіть у систему та змініть його якнайшвидше.",
                    "",
                    "З повагою,",
                    "Команда Lumina");
        } else {
            subject = "Lumina — Your new password";
            body = String.join("\n",
                    "Hello " + patient.getName() + ",",
                    "",
                    "Your password has been reset per your request.",
                    "Your new password is:",
                    "",
                    "    " + newPassword,
                    "",
                    "Please log in and change it as soon as possible.",
                    "",
                    "Best,",
                    "Lumina Team");
        }

        emailService.sendSimpleMessage(patient.getEmail(), subject, body);
    }

    public void updateCoachEmail(String patientId, String coachEmail) {

        Patient patient = patientRepository.getPatientById(patientId);

        if (patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }

        patient.setCoachEmail(coachEmail);
        patientRepository.save(patient);
    }

}
