package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Log;
import ch.uzh.ifi.imrg.patientapp.repository.LogRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.LogOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.LogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void createLog(String patientId, LogTypes logType, String uniqueIdentifier) {
        Log log = new Log();
        log.setPatientId(patientId);
        log.setLogType(logType);
        log.setTimestamp(Instant.now());
        log.setUniqueIdentifier(uniqueIdentifier);

        // Assuming there's a repository to save the log
        logRepository.save(log);
    }

    public List<LogOutputDTO> getLogsByPatientIdAndLogType(String patientId, String logType) {
        LogTypes type = LogTypes.valueOf(logType.toUpperCase());
        List<Log> logs = logRepository.findByPatientIdAndLogType(patientId, type);

        return LogMapper.INSTANCE.convertEntitiesToLogOutputDTOs(logs);
    }
}
