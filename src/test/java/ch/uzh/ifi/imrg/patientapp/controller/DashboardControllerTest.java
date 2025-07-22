package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestsOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private PsychologicalTestService psychologicalTestService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DashboardController controller;

    @Test
    void getExerciseOverview_shouldCallServiceAndReturnList() {
        // Arrange
        Patient patient = new Patient();
        ExercisesOverviewOutputDTO e1 = new ExercisesOverviewOutputDTO();
        ExercisesOverviewOutputDTO e2 = new ExercisesOverviewOutputDTO();
        List<ExercisesOverviewOutputDTO> expected = List.of(e1, e2);

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(exerciseService.getExercisesForDashboard(patient)).thenReturn(expected);

        // Act
        List<ExercisesOverviewOutputDTO> result = controller.getExerciseOverview(request);

        // Assert
        assertEquals(expected, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExercisesForDashboard(patient);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void getPsychologicalTestsOverview_shouldCallServiceAndReturnList() {
        // Arrange
        Patient patient = new Patient();
        PsychologicalTestsOverviewOutputDTO t1 = new PsychologicalTestsOverviewOutputDTO();
        PsychologicalTestsOverviewOutputDTO t2 = new PsychologicalTestsOverviewOutputDTO();
        List<PsychologicalTestsOverviewOutputDTO> expected = List.of(t1, t2);

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(psychologicalTestService.getPsychologicalTestsForDashboard(patient)).thenReturn(expected);

        // Act
        List<PsychologicalTestsOverviewOutputDTO> result = controller.getPsychologicalTestsOverview(request);

        // Assert
        assertEquals(expected, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(psychologicalTestService).getPsychologicalTestsForDashboard(patient);
        verifyNoMoreInteractions(patientService, psychologicalTestService);
    }
}
