package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameAndPatientIdOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void createPsychologicalTest_shouldCallServices() throws Exception {
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

    @Test
    void getAllTestsForPatient_shouldReturnDTOs() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("patient123");
        List<PsychologicalTestNameAndPatientIdOutputDTO> expectedList = List.of(
                new PsychologicalTestNameAndPatientIdOutputDTO("Test A", "patient123"),
                new PsychologicalTestNameAndPatientIdOutputDTO("Test B", "patient123")
        );

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(psychologicalTestService.getAllTestNamesForPatient(patient, patient.getId())).thenReturn(expectedList);

        // Act
        List<PsychologicalTestNameAndPatientIdOutputDTO> result = controller.getPsychologicalTestNames(request, patient.getId());

        // Assert
        assertEquals(expectedList, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(psychologicalTestService).getAllTestNamesForPatient(patient, patient.getId());
        verifyNoMoreInteractions(patientService, psychologicalTestService);
    }

    @Test
    void getPsychologicalTestResults_shouldReturnResults() {
        // Arrange
        String patientId = "123";
        String testName = "DepressionTest";
        Patient loggedInPatient = new Patient();
        List<PsychologicalTestOutputDTO> expectedResults = List.of(new PsychologicalTestOutputDTO("DepressionTest"));

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(loggedInPatient);
        when(psychologicalTestService.getPsychologicalTestResults(loggedInPatient, patientId, testName))
                .thenReturn(expectedResults);

        // Act
        List<PsychologicalTestOutputDTO> result =
                controller.getPsychologicalTestResults(request, patientId, testName);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(psychologicalTestService).getPsychologicalTestResults(loggedInPatient, patientId, testName);
        verifyNoMoreInteractions(patientService, psychologicalTestService);

        assertEquals(expectedResults, result);
    }

}
