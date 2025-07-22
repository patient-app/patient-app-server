package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@ActiveProfiles("test")
@DataJpaTest
public class PatientRepositoryCascadeTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Test
    void deletingPatientAlsoDeletesExcercise() {
        Patient patient = new Patient();
        patient.setId("Id1");
        patient.setEmail("test@gg.com");
        patient.setPassword("savePW");
        patient.setCoachAccessKey("saveKey");
        patient = patientRepository.saveAndFlush(patient);

        Exercise exercise = new Exercise();
        exercise.setId("exId");
        exercise.setPatient(patient);
        exerciseRepository.saveAndFlush(exercise);

        entityManager.flush();
        entityManager.clear();

        assertThat(patientRepository.existsById(patient.getId()));
        assertThat(exerciseRepository.existsById(exercise.getId()));

        patientRepository.deleteById(patient.getId());
        patientRepository.flush();

        assertThat(patientRepository.existsById(patient.getId())).isFalse();
        assertThat(exerciseRepository.existsById(exercise.getId())).isFalse();

    }

    @Test
    void deletingPatientAlsoDeletesConversation() {
        Patient patient = new Patient();
        patient.setId("Id1");
        patient.setEmail("test@gg.com");
        patient.setPassword("savePW");
        patient.setCoachAccessKey("saveKey");
        patient = patientRepository.saveAndFlush(patient);

        GeneralConversation conversation = new GeneralConversation();
        conversation.setId("convoId");
        conversation.setPatient(patient);
        conversationRepository.saveAndFlush(conversation);

        entityManager.flush();
        entityManager.clear();

        assertThat(patientRepository.existsById(patient.getId()));
        assertThat(conversationRepository.existsById(conversation.getId()));

        patientRepository.deleteById(patient.getId());
        patientRepository.flush();

        assertThat(patientRepository.existsById(patient.getId())).isFalse();
        assertThat(conversationRepository.existsById(conversation.getId())).isFalse();

    }

    @Test
    void deletingPatientAlsoDeletesJournalEntry() {
        Patient patient = new Patient();
        patient.setId("Id1");
        patient.setEmail("test@gg.com");
        patient.setPassword("savePW");
        patient.setCoachAccessKey("saveKey");
        patient = patientRepository.saveAndFlush(patient);

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setId("jeId");
        journalEntry.setPatient(patient);
        journalEntry.setTitle("title");
        journalEntryRepository.saveAndFlush(journalEntry);

        entityManager.flush();
        entityManager.clear();

        assertThat(patientRepository.existsById(patient.getId()));
        assertThat(journalEntryRepository.existsById(journalEntry.getId()));

        patientRepository.deleteById(patient.getId());
        patientRepository.flush();

        assertThat(patientRepository.existsById(patient.getId())).isFalse();
        assertThat(journalEntryRepository.existsById(journalEntry.getId())).isFalse();

    }

    @Test
    void deletingPatientAlsoDeletesMeeting() {
        Patient patient = new Patient();
        patient.setId("Id1");
        patient.setEmail("test@gg.com");
        patient.setPassword("savePW");
        patient.setCoachAccessKey("saveKey");
        patient = patientRepository.saveAndFlush(patient);

        Meeting meeting = new Meeting();
        meeting.setMeetingStatus(MeetingStatus.PENDING);
        meeting.setId("MeetId");
        meeting.setId("id1");
        meeting.setStartAt(Instant.parse("2025-06-26T10:15:30+02:00"));
        meeting.setEndAt(Instant.parse("2025-06-26T11:45:15.500+02:00"));
        meeting.setPatient(patient);
        meetingRepository.saveAndFlush(meeting);

        entityManager.flush();
        entityManager.clear();

        assertThat(patientRepository.existsById(patient.getId()));
        assertThat(meetingRepository.existsById(meeting.getId()));

        patientRepository.deleteById(patient.getId());
        patientRepository.flush();

        assertThat(patientRepository.existsById(patient.getId())).isFalse();
        assertThat(meetingRepository.existsById(meeting.getId())).isFalse();

    }
}
