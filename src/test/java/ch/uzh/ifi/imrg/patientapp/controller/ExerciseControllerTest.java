package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseCompletionNameInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentResultInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
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

    @Test
    void testGetExerciseChatbot_ReturnsOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exerciseChatbot123";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExerciseChatbotOutputDTO expectedDTO = new ExerciseChatbotOutputDTO();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.getExerciseChatbot(exerciseId, mockPatient)).thenReturn(expectedDTO);

        // Act
        ExerciseChatbotOutputDTO result = exerciseController.getExerciseChatbot(request, exerciseId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExerciseChatbot(exerciseId, mockPatient);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testGetOneExerciseOverview_ReturnsOverviewList() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExecutionOverviewOutputDTO dto1 = new ExecutionOverviewOutputDTO();
        ExecutionOverviewOutputDTO dto2 = new ExecutionOverviewOutputDTO();
        List<ExecutionOverviewOutputDTO> expectedList = List.of(dto1, dto2);

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.getOneExercisesOverview(mockPatient, exerciseId)).thenReturn(expectedList);

        // Act
        List<ExecutionOverviewOutputDTO> result = exerciseController.getOneExerciseOverview(request, exerciseId);

        // Assert
        assertEquals(expectedList, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getOneExercisesOverview(mockPatient, exerciseId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }


    @Test
    void testGetExerciseExecution_ReturnsExerciseOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";
        String exerciseExecutionId = "exec456";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExerciseOutputDTO expectedDTO = new ExerciseOutputDTO();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.getExerciseExecution(exerciseId, mockPatient, exerciseExecutionId)).thenReturn(expectedDTO);

        // Act
        ExerciseOutputDTO result = exerciseController.getExerciseExecution(request, exerciseId, exerciseExecutionId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).getExerciseExecution(exerciseId, mockPatient, exerciseExecutionId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testStartExercise_ReturnsStartOutputDTO() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        ExerciseStartOutputDTO expectedDTO = new ExerciseStartOutputDTO();

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);
        when(exerciseService.startExercise(exerciseId, mockPatient)).thenReturn(expectedDTO);

        // Act
        ExerciseStartOutputDTO result = exerciseController.startExercise(request, exerciseId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).startExercise(exerciseId, mockPatient);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testPostExerciseCompletionName_InvokesServiceCorrectly() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";
        ExerciseCompletionNameInputDTO inputDTO = new ExerciseCompletionNameInputDTO();

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        exerciseController.postExerciseCompletionName(request, exerciseId, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).setExerciseCompletionName(mockPatient, exerciseId, inputDTO);
        verifyNoMoreInteractions(patientService, exerciseService);
    }

    @Test
    void testPutExerciseCompletionName_InvokesServiceCorrectly() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";
        ExerciseCompletionNameInputDTO inputDTO = new ExerciseCompletionNameInputDTO();

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        exerciseController.putExerciseCompletionName(request, exerciseId, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).setExerciseCompletionName(mockPatient, exerciseId, inputDTO);
        verifyNoMoreInteractions(patientService, exerciseService);
    }


    @Test
    void testPostExerciseComponentResult_InvokesServiceCorrectly() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";
        String exerciseComponentId = "component456";
        ExerciseComponentResultInputDTO inputDTO = new ExerciseComponentResultInputDTO();

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        exerciseController.postExerciseComponentResult(request, exerciseId, exerciseComponentId, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).setExerciseComponentResult(mockPatient, exerciseId, inputDTO, exerciseComponentId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }


    @Test
    void testPutExerciseComponentResult_InvokesServiceCorrectly() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String exerciseId = "exercise123";
        String exerciseComponentId = "component456";
        ExerciseComponentResultInputDTO inputDTO = new ExerciseComponentResultInputDTO();

        Patient mockPatient = new Patient();
        mockPatient.setId("p123");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(mockPatient);

        // Act
        exerciseController.putExerciseComponentResult(request, exerciseId, exerciseComponentId, inputDTO);

        // Assert
        verify(patientService).getCurrentlyLoggedInPatient(request);
        verify(exerciseService).setExerciseComponentResult(mockPatient, exerciseId, inputDTO, exerciseComponentId);
        verifyNoMoreInteractions(patientService, exerciseService);
    }



}
