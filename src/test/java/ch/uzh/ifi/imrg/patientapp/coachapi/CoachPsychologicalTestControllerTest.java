package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        PsychologicalTestNameOutputDTO test1 = new PsychologicalTestNameOutputDTO();
        test1.setName("Test1");
        PsychologicalTestNameOutputDTO test2 = new PsychologicalTestNameOutputDTO();
        test2.setName("Test2");
        List<PsychologicalTestNameOutputDTO> expected = List.of(test1, test2);

        when(psychologicalTestService.getAllTestNamesForPatient(patientId)).thenReturn(expected);

        // Act
        List<PsychologicalTestNameOutputDTO> result = controller.getPsychologicalTestNames(patientId);

        // Assert
        assertEquals(expected, result);
        verify(psychologicalTestService).getAllTestNamesForPatient(patientId);
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

        when(psychologicalTestService.getPsychologicalTestResults(patientId, testName)).thenReturn(expected);

        // Act
        List<PsychologicalTestOutputDTO> result = controller.getPsychologicalTestResults(patientId, testName);

        // Assert
        assertEquals(expected, result);
        verify(psychologicalTestService).getPsychologicalTestResults(patientId, testName);
        verifyNoMoreInteractions(psychologicalTestService);
    }

}
