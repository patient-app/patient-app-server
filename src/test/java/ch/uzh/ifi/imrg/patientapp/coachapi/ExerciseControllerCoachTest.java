package ch.uzh.ifi.imrg.patientapp.coachapi;


import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseControllerCoachTest {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseControllerCoach exerciseControllerCoach;

    @Test
    void testCreateExercise_CallsExerciseService() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String patientId = "patient123";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        // Act
        exerciseControllerCoach.createExercise(request, patientId, inputDTO);

        // Assert
        verify(exerciseService).createExercise(patientId, inputDTO);
        verifyNoMoreInteractions(exerciseService);
    }
}
