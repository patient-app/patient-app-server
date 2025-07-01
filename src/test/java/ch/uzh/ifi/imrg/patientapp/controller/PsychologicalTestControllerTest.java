package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PsychologicalTestControllerTest {
    @Mock
    private PatientService patientService;

    @Mock
    private PsychologicalTestService psychologicalTestService;

    @InjectMocks
    private PsychologicalTestController controller;

    @Mock
    private HttpServletRequest request;

    @Test
    void createPsychologicalTest_shouldCallServices() {
        // Arrange
        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();
        Patient patient = new Patient();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        // Act
        controller.createPsychologicalTest(request, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(psychologicalTestService).createPsychologicalTest(patient, inputDTO);
        verifyNoMoreInteractions(patientService, psychologicalTestService);
    }
}
