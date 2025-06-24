package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutLanguageDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutOnboardedDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {
    @Mock
    private PatientService patientService;
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
}
