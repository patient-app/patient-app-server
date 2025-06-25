package ch.uzh.ifi.imrg.patientapp.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {

    Optional<Meeting> findByExternalMeetingId(String externalMeetingId);

    boolean existsByExternalMeetingId(String externalMeetingId);

    List<Meeting> findByPatientId(String patientId);

    List<Meeting> findByPatientIdAndStartAtAfter(String patientId, OffsetDateTime from);

}
