package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.LoginPatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



public class PatientControllerTest {

    private PatientService patientService;
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        patientService = mock(PatientService.class);
        patientController = new PatientController(patientService);
    }

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
}
