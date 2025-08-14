package ch.uzh.ifi.imrg.patientapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import java.util.Set;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {

    List<JournalEntry> findByPatientId(String patientId);

    List<JournalEntry> findAllByPatientIdAndSharedWithTherapistTrue(String patientId);

    Optional<JournalEntry> findByIdAndSharedWithTherapistTrue(String id);

    @Query("""
            select distinct t
            from JournalEntry e
            join e.tags t
            where e.patient.id = :patientId
            """)
    Set<String> findDistinctTagsByPatientId(@Param("patientId") String patientId);

    List<JournalEntry> findByPatientIdOrderByUpdatedAtDesc(String patientId);
}
