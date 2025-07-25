package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.constant.ChatBotAvatar;
import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {
    @Mock
    private PatientService patientService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private LogService logService;

    @InjectMocks
    private PatientController patientController;

    @Test
    void getCurrentlyLoggedInPatient_shouldReturnMappedDTO() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        PatientOutputDTO dto = patientController.getCurrentlyLoggedInPatient(request);

        assertNotNull(dto);
    }

    @Test
    void registerPatient_shouldConvertDTOAndReturnCreatedDTO() {
        CreatePatientDTO input = new CreatePatientDTO();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Patient mockPatient = new Patient();
        when(patientService.registerPatient(any(), eq(request), eq(response))).thenReturn(mockPatient);

        PatientOutputDTO dto = patientController.registerPatient(input, request, response);

        assertNotNull(dto);
    }

    @Test
    void loginTherapist_shouldReturnLoggedInPatientDTO() {
        LoginPatientDTO loginDTO = new LoginPatientDTO();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Patient mockPatient = new Patient();
        when(patientService.loginPatient(loginDTO, request, response)).thenReturn(mockPatient);

        PatientOutputDTO dto = patientController.loginTherapist(loginDTO, request, response);

        assertNotNull(dto);
    }

    @Test
    void logoutTherapist_shouldCallService() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        // No exception expected
        assertDoesNotThrow(() -> patientController.logoutTherapist(response));
        verify(patientService).logoutPatient(response);
    }

    @Test
    void setLanguage_shouldUpdateLanguageFieldAndCallSetField() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        PutLanguageDTO dto = new PutLanguageDTO();
        dto.setLanguage("en");

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        patientController.setLanguage(dto, request);

        // Assert
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientService).setField(patientCaptor.capture());

        Patient captured = patientCaptor.getValue();
        assertNotNull(captured);
        assert captured.getLanguage().equals("en");
    }

    @Test
    void getLanguage_shouldReturnPatientOutputDTO() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        Patient mockPatient = new Patient();
        mockPatient.setLanguage("de"); // Set some language for clarity

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        PatientOutputDTO result = patientController.getLanguage(request);

        // Assert
        assertNotNull(result);
        assertEquals("de", result.getLanguage());
    }

    @Test
    void setOnboarded_shouldUpdateWhenNotAlreadyOnboarded() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        PutOnboardedDTO dto = new PutOnboardedDTO();
        dto.setOnboarded(true);

        Patient mockPatient = new Patient();
        mockPatient.setOnboarded(false); // Not onboarded

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        patientController.setOnboarded(dto, request);

        // Assert
        verify(patientService).setField(mockPatient);
        assertTrue(mockPatient.isOnboarded());
    }

    @Test
    void setOnboarded_shouldNotUpdateWhenAlreadyOnboarded() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        PutOnboardedDTO dto = new PutOnboardedDTO();
        dto.setOnboarded(false); // Doesn't matter – should be ignored

        Patient mockPatient = new Patient();
        mockPatient.setOnboarded(true); // Already onboarded

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        patientController.setOnboarded(dto, request);

        // Assert
        verify(patientService, never()).setField(any());
        assertTrue(mockPatient.isOnboarded()); // Still true
    }

    @Test
    void getOnboarded_shouldReturnPatientOutputDTOWithCorrectOnboardingStatus() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        Patient mockPatient = new Patient();
        mockPatient.setOnboarded(true); // or false to test the other value

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        PatientOutputDTO result = patientController.getOnboarded(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isOnboarded()); // or assertFalse(...) if you set false above
    }

    @Test
    void setName_shouldUpdateNameFieldAndCallSetField() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        String name = "myName";
        PutNameDTO dto = new PutNameDTO();
        dto.setName(name);

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        patientController.setName(dto, request);

        // Assert
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientService).setField(patientCaptor.capture());

        Patient captured = patientCaptor.getValue();
        assertNotNull(captured);
        assert captured.getName().equals(name);
    }

    @Test
    void getName_shouldReturnPatientOutputDTO() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Arrange
        String name = "myName";
        Patient mockPatient = new Patient();
        mockPatient.setName(name); // Set some name for clarity

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        PatientOutputDTO result = patientController.getName(request);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    void changePassword_shouldCallService_andReturnNoException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPwd");
        dto.setNewPassword("newPwd");

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        doNothing().when(patientService).changePassword(mockPatient, dto);

        assertDoesNotThrow(() -> patientController.changePassword(dto, request));
        verify(patientService).changePassword(mockPatient, dto);
    }

    @Test
    void changePassword_shouldThrowForbidden_whenOldPasswordIncorrect() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("badOld");
        dto.setNewPassword("newPwd");

        Patient mockPatient = new Patient();
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Old password is incorrect"))
                .when(patientService).changePassword(mockPatient, dto);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> patientController.changePassword(dto, request));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void resetPassword_shouldCallService_andReturnNoException() {
        // Arrange
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail("patient@example.com");

        // service does nothing (void)
        doNothing().when(patientService).resetPasswordAndNotify(dto.getEmail());

        // Act & Assert
        assertDoesNotThrow(() -> patientController.resetPassword(dto));
        verify(patientService).resetPasswordAndNotify(dto.getEmail());
    }

    @Test
    void resetPassword_whenPatientNotFound_shouldPropagateException() {
        // Arrange
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail("unknown@example.com");

        // simulate not found
        doThrow(new EntityNotFoundException("No patient found with email: " + dto.getEmail()))
                .when(patientService).resetPasswordAndNotify(dto.getEmail());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> patientController.resetPassword(dto));
        assertTrue(ex.getMessage().contains(dto.getEmail()));
    }

    @Test
    void setAvatar_shouldUpdateAvatarAndLogChange() {
        PutAvatarDTO dto = new PutAvatarDTO();
        dto.setChatBotAvatar(ChatBotAvatar.ANIMALISTIC);

        Patient patient = new Patient();
        patient.setId("p1");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        patientController.setAvatar(dto, request);

        assertEquals(ChatBotAvatar.ANIMALISTIC, patient.getChatBotAvatar());
        verify(patientService).setField(patient);
        verify(logService).createLog("p1", LogTypes.CHATBOT_ICON_UPDATE, null, "");
    }

    @Test
    void getAvatar_shouldReturnMappedDTO() {
        Patient patient = new Patient();
        patient.setId("p1");
        patient.setEmail("alice@example.com");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        PatientOutputDTO dto = patientController.getAvatar(request);

        assertNotNull(dto);
        assertEquals("p1", dto.getId());
        assertEquals("alice@example.com", dto.getEmail());
    }


}
