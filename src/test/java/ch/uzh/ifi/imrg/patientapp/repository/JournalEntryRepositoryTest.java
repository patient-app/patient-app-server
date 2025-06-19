package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class JournalEntryRepositoryTest {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findDistinctTagsByPatientId_returnsUniqueTagsForPatient() {
        // Persist patient
        Patient patient = new Patient();
        patient.setId("myId");
        patient.setEmail("me@example.com");
        patient.setPassword("pwd");
        entityManager.persist(patient);

        // First journal entry
        JournalEntry e1 = new JournalEntry();
        e1.setPatient(patient);
        e1.setTitle("t1");
        e1.setContent("c1");
        e1.getTags().add("a");
        e1.getTags().add("b");
        entityManager.persist(e1);

        // Second journal entry
        JournalEntry e2 = new JournalEntry();
        e2.setPatient(patient);
        e2.setTitle("t2");
        e2.setContent("c2");
        e2.getTags().add("b");
        e2.getTags().add("c");
        entityManager.persist(e2);

        entityManager.flush();
        entityManager.clear();

        // When
        Set<String> tags = journalEntryRepository.findDistinctTagsByPatientId(patient.getId());

        // Then
        assertThat(tags).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void findTagsByPatientId_returnsOnlyThatPatientTags() {
        // given
        Patient patient = new Patient();
        patient.setId("p1");
        patient.setEmail("p1@example.com");
        patient.setPassword("pwd");
        entityManager.persist(patient);

        Patient other = new Patient();
        other.setId("p2");
        other.setEmail("p2@example.com");
        other.setPassword("pwd");
        entityManager.persist(other);

        // entry for p1
        JournalEntry e1 = new JournalEntry();
        e1.setPatient(patient);
        e1.setTitle("A");
        e1.setContent("C");
        e1.getTags().add("a");
        entityManager.persist(e1);

        // another entry for p1
        JournalEntry e2 = new JournalEntry();
        e2.setPatient(patient);
        e2.setTitle("B");
        e2.setContent("D");
        e2.getTags().add("b");
        entityManager.persist(e2);

        // entry for p2
        JournalEntry e3 = new JournalEntry();
        e3.setPatient(other);
        e3.setTitle("X");
        e3.setContent("Y");
        e3.getTags().add("x");
        e3.getTags().add("a");
        entityManager.persist(e3);

        entityManager.flush();
        entityManager.clear();

        // when
        Set<String> tags = journalEntryRepository.findDistinctTagsByPatientId(patient.getId());

        // then
        assertThat(tags).containsExactlyInAnyOrder("a", "b").doesNotContain("x");

    }
}
