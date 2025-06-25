package ch.uzh.ifi.imrg.patientapp.coachapi;


import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
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
class CoachExerciseControllerTest {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private CoachExerciseController coachExerciseController;

    @Test
    void testCreateExercise_CallsExerciseService() {
        // Arrange
        String patientId = "patient123";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        // Act
        coachExerciseController.createExercise(patientId, inputDTO);

        // Assert
        verify(exerciseService).createExercise(patientId, inputDTO);
        verifyNoMoreInteractions(exerciseService);
    }
    @Test
    void testGetAllExercises_CallsExerciseService() {
        // Arrange
        String patientId = "patient123";
        List<ExercisesOverviewOutputDTO> mockList = List.of(new ExercisesOverviewOutputDTO());
        when(exerciseService.getAllExercisesForCoach(patientId)).thenReturn(mockList);

        // Act
        List<ExercisesOverviewOutputDTO> result = coachExerciseController.getAllExercises(patientId);

        // Assert
        verify(exerciseService).getAllExercisesForCoach(patientId);
        assertEquals(mockList, result);
        verifyNoMoreInteractions(exerciseService);
    }


    @Test
    void testDeleteExercise_CallsExerciseService() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";

        // Act
        coachExerciseController.deleteExercise(patientId, exerciseId);

        // Assert
        verify(exerciseService).deleteExercise(patientId, exerciseId);
        verifyNoMoreInteractions(exerciseService);
    }

    @Test
    void testUpdateExercise_CallsUpdateExerciseOnService() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        // Act
        coachExerciseController.updateExercise(patientId, exerciseId, inputDTO);

        // Assert
        verify(exerciseService).updateExercise(patientId, exerciseId, inputDTO);
        verifyNoMoreInteractions(exerciseService);
    }


}
