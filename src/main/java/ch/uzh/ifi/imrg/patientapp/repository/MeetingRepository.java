package ch.uzh.ifi.imrg.patientapp.repository;

import java.time.Instant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {

    List<Meeting> findByPatientId(String patientId);
    List<Meeting> findByPatientIdOrderByStartAtAsc(String patientId);

    List<Meeting> findByPatientIdAndStartAtAfter(String patientId, Instant from);

}
