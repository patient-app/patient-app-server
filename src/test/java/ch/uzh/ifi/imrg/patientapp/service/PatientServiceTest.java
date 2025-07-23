package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.ChangePasswordDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import ch.uzh.ifi.imrg.patientapp.utils.JwtUtil;
import ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {
    @Mock
    PatientRepository patientRepository;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    private EmailService emailService;

    @InjectMocks
    PatientService patientService;

    private static final String EMAIL = "test@example.com";

    @Test
    void getCurrentlyLoggedInPatient_shouldReturnPatient_whenExists() {
        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateJWTAndExtractEmail(request)).thenReturn(EMAIL);
            when(patientRepository.getPatientByEmail(EMAIL)).thenReturn(patient);

            Patient result = patientService.getCurrentlyLoggedInPatient(request);
            assertEquals(patient, result);
        }
    }

    @Test
    void getCurrentlyLoggedInPatient_shouldThrow_whenPatientNotFound() {
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateJWTAndExtractEmail(request)).thenReturn(EMAIL);
            when(patientRepository.getPatientByEmail(EMAIL)).thenReturn(null);

            assertThrows(ResponseStatusException.class,
                    () -> patientService.getCurrentlyLoggedInPatient(request));
        }
    }

    @Test
    void addConversationToPatient_shouldAddAndSave() {
        Patient patient = new Patient();
        patient.setConversations(new ArrayList<>());
        GeneralConversation conversation = new GeneralConversation();

        Patient saved = new Patient();
        when(patientRepository.save(patient)).thenReturn(saved);

        Patient result = patientService.addConversationToPatient(patient, conversation);
        assertTrue(patient.getConversations().contains(conversation));
        assertEquals(conversation, result.getConversations().get(0));
    }

    @Test
    void loginPatient_shouldReturnPatient_whenCredentialsAreCorrect() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String encryptedPassword = "encrypted-password";
        String jwt = "mock-jwt";

        LoginPatientDTO loginDTO = new LoginPatientDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        Patient mockPatient = new Patient();
        mockPatient.setEmail(email);
        mockPatient.setPassword(encryptedPassword);

        when(patientRepository.getPatientByEmail(email)).thenReturn(mockPatient);
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = mockStatic(PasswordUtil.class);
                MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {

            mockedPasswordUtil.when(() -> PasswordUtil.checkPassword(password, encryptedPassword)).thenReturn(true);
            mockedJwtUtil.when(() -> JwtUtil.createJWT(email)).thenReturn(jwt);
            mockedJwtUtil.when(() -> JwtUtil.addJwtCookie(any(), any(), eq(jwt))).then(invocation -> null);

            // Act
            Patient result = patientService.loginPatient(loginDTO, mock(HttpServletRequest.class),
                    mock(HttpServletResponse.class));

            // Assert
            assertNotNull(result);
            assertEquals(email, result.getEmail());
            verify(patientRepository).getPatientByEmail(email);
            mockedPasswordUtil.verify(() -> PasswordUtil.checkPassword(password, encryptedPassword));
            mockedJwtUtil.verify(() -> JwtUtil.createJWT(email));
        }
    }

    @Test
    void registerPatient_shouldCreateAndReturnPatient() {
        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setPassword("plain");

        when(patientRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(patientRepository.save(any())).thenReturn(patient);

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class);
                MockedStatic<CryptographyUtil> cryptoMock = mockStatic(CryptographyUtil.class);
                MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {

            pwMock.when(() -> PasswordUtil.encryptPassword("plain")).thenReturn("hashed");
            cryptoMock.when(CryptographyUtil::generatePrivateKey).thenReturn("key");
            cryptoMock.when(() -> CryptographyUtil.encrypt("key")).thenReturn("encrypted-key");
            jwtMock.when(() -> JwtUtil.createJWT(EMAIL)).thenReturn("jwt");

            Patient result = patientService.registerPatient(patient, request, response);
            assertEquals("hashed", result.getPassword());
            assertEquals("encrypted-key", result.getPrivateKey());

            jwtMock.verify(() -> JwtUtil.addJwtCookie(response, request, "jwt"));
        }
    }

    @Test
    void registerPatient_shouldThrow_whenEmailExists() {
        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setPassword("abc");

        when(patientRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(Error.class, () -> patientService.registerPatient(patient, request, response));
    }

    @Test
    void loginPatient_shouldSucceed_whenCredentialsValid() {
        LoginPatientDTO loginDto = new LoginPatientDTO();
        loginDto.setEmail(EMAIL);
        loginDto.setPassword("abc");

        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setPassword("hashed");

        when(patientRepository.getPatientByEmail(EMAIL)).thenReturn(patient);

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class);
                MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {

            pwMock.when(() -> PasswordUtil.checkPassword("abc", "hashed")).thenReturn(true);
            jwtMock.when(() -> JwtUtil.createJWT(EMAIL)).thenReturn("jwt");

            Patient result = patientService.loginPatient(loginDto, request, response);
            assertEquals(patient, result);

            jwtMock.verify(() -> JwtUtil.addJwtCookie(response, request, "jwt"));
        }
    }

    @Test
    void loginPatient_shouldThrow_whenWrongPassword() {
        LoginPatientDTO loginDto = new LoginPatientDTO();
        loginDto.setEmail(EMAIL);
        loginDto.setPassword("abc");

        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setPassword("hashed");

        when(patientRepository.getPatientByEmail(EMAIL)).thenReturn(patient);

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class)) {
            pwMock.when(() -> PasswordUtil.checkPassword("abc", "hashed")).thenReturn(false);

            assertThrows(Error.class, () -> patientService.loginPatient(loginDto, request, response));
        }
    }

    @Test
    void registerPatient_shouldThrowError_whenEmailIsNull() {
        Patient patient = new Patient();
        patient.setPassword("validPassword"); // Only set password, no email

        assertThrows(Error.class,
                () -> patientService.registerPatient(patient, mock(HttpServletRequest.class),
                        mock(HttpServletResponse.class)),
                "Creating patient failed because no email was specified");
    }

    @Test
    void registerPatient_shouldThrowError_whenPasswordIsNull() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com"); // Only set email, no password

        assertThrows(Error.class,
                () -> patientService.registerPatient(patient, mock(HttpServletRequest.class),
                        mock(HttpServletResponse.class)),
                "Creating patient failed because no password was specified");
    }

    @Test
    void registerPatient_shouldThrowError_whenStringIdExistsInRepository() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com");
        patient.setPassword("password123");
        String uuid = UUID.randomUUID().toString(); // String ID
        patient.setId(uuid);

        when(patientRepository.existsById(uuid)).thenReturn(true);

        Error thrown = assertThrows(Error.class, () -> patientService.registerPatient(patient,
                mock(HttpServletRequest.class), mock(HttpServletResponse.class)));
        System.out.println("Actual error message: " + thrown.getMessage());

        assertTrue(thrown.getMessage().contains("Creating client failed because patient with this ID already exists"));
    }

    @Test
    void logoutPatient_shouldRemoveJwtCookie() {
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            patientService.logoutPatient(response);
            jwtMock.verify(() -> JwtUtil.removeJwtCookie(response));
        }
    }

    @Test
    void setField_shouldSavePatient() {
        // Arrange
        Patient patient = new Patient();
        // optionally set some fields on the patient

        // Act
        patientService.setField(patient);

        // Assert
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void loginPatient_shouldThrowError_WhenPatientNotFound() {
        // Arrange
        String email = "notfound@example.com";
        String password = "password123";
        LoginPatientDTO loginDTO = new LoginPatientDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        when(patientRepository.getPatientByEmail(email)).thenReturn(null);

        // Act + Assert
        Error error = assertThrows(Error.class, () -> patientService.loginPatient(loginDTO, request, response));

        // Verify exception message
        assert (error.getMessage().contains("No client with email: " + email + " exists"));
    }

    @Test
    void changePassword_shouldEncryptAndSave_whenValid() {
        // Arrange
        Patient patient = spy(new Patient());
        patient.setPassword("oldHash");
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPwd");
        dto.setNewPassword("newPwd");

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class)) {
            pwMock.when(() -> PasswordUtil.checkPassword("oldPwd", "oldHash")).thenReturn(true);
            pwMock.when(() -> PasswordUtil.encryptPassword("newPwd")).thenReturn("newHash");

            // Act
            patientService.changePassword(patient, dto);

            // Assert
            pwMock.verify(() -> PasswordUtil.encryptPassword("newPwd"));
            ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(captor.capture());
            assertEquals("newHash", captor.getValue().getPassword());
        }
    }

    @Test
    void changePassword_shouldThrowForbidden_whenOldPasswordIncorrect() {
        // Arrange
        Patient patient = new Patient();
        patient.setPassword("storedHash");
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("wrongOld");
        dto.setNewPassword("newPwd");

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class)) {
            pwMock.when(() -> PasswordUtil.checkPassword("wrongOld", "storedHash")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> patientService.changePassword(patient, dto));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertTrue(ex.getReason().contains("Old password is incorrect"));
        }
    }

    @Test
    void resetPasswordAndNotify_shouldGenerateEncodeSaveAndSendEmail_inEnglish() {
        // Arrange
        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setName("Alice");
        patient.setLanguage("en");
        when(patientRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(patient));

        // Stub the static PasswordUtil calls
        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class)) {
            pwMock.when(() -> PasswordUtil.generatePassword(PasswordUtil.Alphabet.LATIN))
                    .thenReturn("Abc1-Def2-Ghi3-Jkl4");
            pwMock.when(() -> PasswordUtil.encryptPassword("Abc1-Def2-Ghi3-Jkl4"))
                    .thenReturn("encrypted");

            // Act
            patientService.resetPasswordAndNotify(EMAIL);

            // Assert: password was set & saved
            assertEquals("encrypted", patient.getPassword());
            verify(patientRepository).save(patient);

            // Assert: emailService.sendSimpleMessage(...) called with correct args
            ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendSimpleMessage(
                    toCap.capture(),
                    subjCap.capture(),
                    bodyCap.capture());

            assertEquals(EMAIL, toCap.getValue());
            assertEquals("Lumina — Your new password", subjCap.getValue());
            String body = bodyCap.getValue();
            assertTrue(body.contains("Hello Alice,"));
            assertTrue(body.contains("Your new password is:"));
            assertTrue(body.contains("    Abc1-Def2-Ghi3-Jkl4"));
        }
    }

    @Test
    void resetPasswordAndNotify_shouldGenerateEncodeSaveAndSendEmail_inUkrainian() {
        // Arrange
        Patient patient = new Patient();
        patient.setEmail(EMAIL);
        patient.setName("Олена");
        patient.setLanguage("uk");
        when(patientRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(patient));

        try (MockedStatic<PasswordUtil> pwMock = mockStatic(PasswordUtil.class)) {
            pwMock.when(() -> PasswordUtil.generatePassword(PasswordUtil.Alphabet.CYRILLIC))
                    .thenReturn("Аб1г-Де2ж-Зи3к-Лм4н");
            pwMock.when(() -> PasswordUtil.encryptPassword("Аб1г-Де2ж-Зи3к-Лм4н"))
                    .thenReturn("encryptedUKR");

            // Act
            patientService.resetPasswordAndNotify(EMAIL);

            // Assert save
            assertEquals("encryptedUKR", patient.getPassword());
            verify(patientRepository).save(patient);

            // Assert email params
            ArgumentCaptor<String> subjCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendSimpleMessage(
                    eq(EMAIL),
                    subjCap.capture(),
                    bodyCap.capture());

            assertEquals("Lumina — Ваш новий пароль", subjCap.getValue());
            String body = bodyCap.getValue();
            assertTrue(body.contains("Привіт, Олена!"));
            assertTrue(body.contains("Ваш новий пароль:"));
            assertTrue(body.contains("    Аб1г-Де2ж-Зи3к-Лм4н"));
        }
    }

    @Test
    void resetPasswordAndNotify_shouldThrow_whenPatientNotFound() {
        // Arrange
        when(patientRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> patientService.resetPasswordAndNotify(EMAIL));
        assertTrue(ex.getMessage().contains("No patient found with email: " + EMAIL));
    }

    @Test
    void updateCoachEmail_shouldSetEmailAndSave_whenPatientExists() {
        // Arrange
        String patientId = "p123";
        String newCoachEmail = "coach@lumina.com";
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setCoachEmail("old@lumina.com");

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        // Act
        patientService.updateCoachEmail(patientId, newCoachEmail);

        // Assert
        assertEquals(newCoachEmail, patient.getCoachEmail(), "Coach email should be updated on the entity");
        verify(patientRepository).save(patient);
    }

    @Test
    void updateCoachEmail_shouldThrowNotFound_whenPatientDoesNotExist() {
        // Arrange
        String patientId = "unknown";
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> patientService.updateCoachEmail(patientId, "any@coach.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Should return 404 when patient is missing");
        assertTrue(ex.getReason().contains("Patient not found"), "Exception reason should mention 'Patient not found'");
    }
}
