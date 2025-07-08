package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseMediaOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

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
class ExerciseControllerTest {
    @Mock
    private PatientService patientService;

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseController exerciseController;

    @Test
    void testGetExerciseOverview_ReturnsOverviewList() {
        // mock request coming from Spring
        HttpServletRequest request = mock(HttpServletRequest.class);

        // mocked domain objects
        Patient patient = new Patient();
        patient.setId("p123");

        ExercisesOverviewOutputDTO dto1 = new ExercisesOverviewOutputDTO();
        ExercisesOverviewOutputDTO dto2 = new ExercisesOverviewOutputDTO();
        List<ExercisesOverviewOutputDTO> expected = List.of(dto1, dto2);

        /* --- stubbing --- */
        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(exerciseService.getExercisesOverview(patient)).thenReturn(expected);

        /* --- invoke controller method --- */
        List<ExercisesOverviewOutputDTO> result = exerciseController.getExerciseOverview(request);

        /* --- assertions & verifications --- */
        assertEquals(expected, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExercisesOverview(patient);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testGetExercise() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise456";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExerciseOutputDTO expectedDTO = new ExerciseOutputDTO();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.getExercise(exerciseId)).thenReturn(expectedDTO);

        // Act
        ExerciseOutputDTO result = exerciseController.getExercise(request, exerciseId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExercise(exerciseId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testGetPictureMock_ReturnsExerciseMediaOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise789";
        String mediaId = "media123";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExerciseMediaOutputDTO expectedDTO = new ExerciseMediaOutputDTO();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.getExerciseMedia(mockPatient, exerciseId, mediaId)).thenReturn(expectedDTO);

        // Act
        ExerciseMediaOutputDTO result = exerciseController.getPicture(request, exerciseId, mediaId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExerciseMedia(mockPatient, exerciseId, mediaId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }
    @Test
    void testPostExerciseFeedback_InvokesServiceCorrectly() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise789";
        ExerciseInformationInputDTO inputDTO = new ExerciseInformationInputDTO();

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        exerciseController.postExerciseFeedback(request, exerciseId, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).putExerciseFeedback(mockPatient, exerciseId, inputDTO);
        verifyNoMoreInteractions(patientService, exerciseService);
    }



}
