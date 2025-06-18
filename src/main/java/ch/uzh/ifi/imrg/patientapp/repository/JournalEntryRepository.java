package ch.uzh.ifi.imrg.patientapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {

    List<JournalEntry> findByPatientId(String patientId);

}
