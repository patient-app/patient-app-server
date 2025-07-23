package ch.uzh.ifi.imrg.patientapp.coachapi;


import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.LogOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoachLogControllerTest {
    @Mock
    private LogService logService;

    @InjectMocks
    private CoachLogController controller;

    @Test
    void listAll_shouldReturnLogsFromService() {
        // Arrange
        String patientId = "patient123";

        LogOutputDTO log1 = new LogOutputDTO();
        log1.setId("1");
        log1.setPatientId(patientId);
        log1.setLogType(LogTypes.PSYCHOLOGICAL_TEST_COMPLETED);
        log1.setTimestamp(Instant.now());
        log1.setUniqueIdentifier("d1");
        LogOutputDTO log2 = new LogOutputDTO();
        log2.setId("2");
        log2.setPatientId(patientId);
        log2.setLogType(LogTypes.PSYCHOLOGICAL_TEST_COMPLETED);
        log2.setTimestamp(Instant.now());
        log2.setUniqueIdentifier("d2");

        List<LogOutputDTO> expectedLogs = List.of(log1, log2);


        when(logService.getLogsByPatientIdAndLogType(patientId, LogTypes.PSYCHOLOGICAL_TEST_COMPLETED)).thenReturn(expectedLogs);

        // Act
        List<LogOutputDTO> result = controller.listAll(patientId, "PSYCHOLOGICAL_TEST_COMPLETED");

        // Assert
        assertEquals(expectedLogs, result);
        verify(logService).getLogsByPatientIdAndLogType(patientId, LogTypes.PSYCHOLOGICAL_TEST_COMPLETED);
        verifyNoMoreInteractions(logService);
    }

}
