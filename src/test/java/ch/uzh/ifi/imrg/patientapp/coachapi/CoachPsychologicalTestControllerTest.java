package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestAssignmentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoachPsychologicalTestControllerTest {
    @Mock
    private PsychologicalTestService psychologicalTestService;

    @InjectMocks
    private CoachPsychologicalTestController controller;

    @Test
    void getPsychologicalTestNames_shouldCallServiceAndReturnList() {
        // Arrange
        String patientId = "patient123";
        PsychologicalTestNameOutputDTO test1 = new PsychologicalTestNameOutputDTO("Test1");
        PsychologicalTestNameOutputDTO test2 = new PsychologicalTestNameOutputDTO("Test2");
        List<PsychologicalTestNameOutputDTO> expected = List.of(test1, test2);

        when(psychologicalTestService.getAllTestNamesForPatientForCoach(patientId)).thenReturn(expected);

        // Act
        List<PsychologicalTestNameOutputDTO> result = controller.getPsychologicalTestNames(patientId);

        // Assert
        assertEquals(expected, result);
        verify(psychologicalTestService).getAllTestNamesForPatientForCoach(patientId);
        verifyNoMoreInteractions(psychologicalTestService);
    }

    @Test
    void getPsychologicalTestResults_shouldCallServiceAndReturnList() {
        // Arrange
        String patientId = "patient123";
        String testName = "Test1";

        PsychologicalTestOutputDTO dto = new PsychologicalTestOutputDTO(testName);
        dto.setDescription("Description");
        dto.setPatientId(patientId);
        dto.setQuestions(List.of());

        List<PsychologicalTestOutputDTO> expected = List.of(dto);

        when(psychologicalTestService.getPsychologicalTestResultsForCoach(patientId, testName)).thenReturn(expected);

        // Act
        List<PsychologicalTestOutputDTO> result = controller.getPsychologicalTestResults(patientId, testName);

        // Assert
        assertEquals(expected, result);
        verify(psychologicalTestService).getPsychologicalTestResultsForCoach(patientId, testName);
        verifyNoMoreInteractions(psychologicalTestService);
    }

    @Test
    void getAvailablePsychologicalTestNames_shouldCallServiceAndReturnList() {
        // Arrange
        String patientId = "patient123";
        PsychologicalTestNameOutputDTO test1 = new PsychologicalTestNameOutputDTO("TestA");
        PsychologicalTestNameOutputDTO test2 = new PsychologicalTestNameOutputDTO("TestB");
        List<PsychologicalTestNameOutputDTO> expected = List.of(test1, test2);

        when(psychologicalTestService.getAllAvailableTestNamesForPatientForCoach(patientId)).thenReturn(expected);

        // Act
        List<PsychologicalTestNameOutputDTO> result = controller.getAvailablePsychologicalTestNames(patientId);

        // Assert
        assertEquals(expected, result);
        verify(psychologicalTestService).getAllAvailableTestNamesForPatientForCoach(patientId);
        verifyNoMoreInteractions(psychologicalTestService);
    }


    @Test
    void createPsychologicalTest_shouldCallService() {
        // Arrange
        String patientId = "patient123";
        String testName = "GAD7";
        PsychologicalTestAssignmentInputDTO dto = new PsychologicalTestAssignmentInputDTO();
        dto.setDoEveryNDays(7);
        dto.setExerciseStart(Instant.now());
        dto.setExerciseEnd(Instant.now().plus(7, ChronoUnit.DAYS));
        dto.setIsPaused(false);

        // Act
        controller.createPsychologicalTest(patientId, testName, dto);

        // Assert
        verify(psychologicalTestService).createPsychologicalTestAssginment(patientId, testName, dto);
        verifyNoMoreInteractions(psychologicalTestService);
    }

    @Test
    void updatePsychologicalTest_shouldCallService() {
        // Arrange
        String patientId = "patient123";
        String testName = "GAD7";
        PsychologicalTestAssignmentInputDTO dto = new PsychologicalTestAssignmentInputDTO();
        dto.setDoEveryNDays(7);
        dto.setExerciseStart(Instant.now());
        dto.setExerciseEnd(Instant.now().plus(7, ChronoUnit.DAYS));
        dto.setIsPaused(true);

        // Act
        controller.updatePsychologicalTest(patientId, testName, dto);

        // Assert
        verify(psychologicalTestService).updatePsychologicalTestAssginment(patientId, testName, dto);
        verifyNoMoreInteractions(psychologicalTestService);
    }


}
