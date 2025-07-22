package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PsychologicalTestsAssignmentRepository  extends JpaRepository<PsychologicalTestAssignment, String> {


    @Query("SELECT a FROM PsychologicalTestAssignment a " +
            "WHERE a.patient = :patient " +
            "AND a.isPaused = false " +
            "AND :now BETWEEN a.exerciseStart AND a.exerciseEnd")
    List<PsychologicalTestAssignment> findActiveAssignments(@Param("patient") Patient patient, @Param("now") Instant now);

}
