package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, String> {
    List<Log> findByPatientIdAndLogType(String patientId, LogTypes logType);
    List<Log> findByPatientIdAndLogTypeAndUniqueIdentifier(String patientId, LogTypes logType, String uniqueIdentifier);
}
