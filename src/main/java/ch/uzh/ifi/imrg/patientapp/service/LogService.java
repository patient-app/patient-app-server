package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Log;
import ch.uzh.ifi.imrg.patientapp.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
}
