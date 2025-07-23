package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Log;
import ch.uzh.ifi.imrg.patientapp.repository.LogRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.LogOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogService logService;

    @Test
    void createLog_shouldSaveLogWithCorrectFields() {
        // Arrange
        String patientId = "p123";
        LogTypes logType = LogTypes.GENERAL_CONVERSATION_MESSAGE_CREATION;
        String uniqueId = "doc123";

        ArgumentCaptor<Log> captor = ArgumentCaptor.forClass(Log.class);

        // Act
        logService.createLog(patientId, logType, uniqueId, "");

        // Assert
        verify(logRepository).save(captor.capture());
        Log savedLog = captor.getValue();
        assertEquals(patientId, savedLog.getPatientId());
        assertEquals(logType, savedLog.getLogType());
        assertEquals(uniqueId, savedLog.getAssociatedEntityId());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    void getLogsByPatientIdAndLogType_shouldReturnMappedDTOs() {
        // Arrange
        String patientId = "p123";
        LogTypes logType = LogTypes.GENERAL_CONVERSATION_MESSAGE_CREATION;

        Log log1 = new Log();
        log1.setId("1");
        log1.setPatientId(patientId);
        log1.setLogType(logType);
        log1.setTimestamp(Instant.now());
        log1.setAssociatedEntityId("x1");

        Log log2 = new Log();
        log2.setId("2");
        log2.setPatientId(patientId);
        log2.setLogType(logType);
        log2.setTimestamp(Instant.now());
        log2.setAssociatedEntityId("x2");

        List<Log> logs = List.of(log1, log2);
        when(logRepository.findByPatientIdAndLogType(patientId, logType)).thenReturn(logs);

        // Act
        List<LogOutputDTO> result = logService.getLogsByPatientIdAndLogType(patientId, logType);

        // Assert
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        verify(logRepository).findByPatientIdAndLogType(patientId, logType);
    }
}
