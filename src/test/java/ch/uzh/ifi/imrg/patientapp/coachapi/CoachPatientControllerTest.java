package ch.uzh.ifi.imrg.patientapp.coachapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.imrg.patientapp.controller.PatientController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class CoachPatientControllerTest {
    @Mock
    private PatientService patientService;
    @InjectMocks
    private PatientController patientController;

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
}
