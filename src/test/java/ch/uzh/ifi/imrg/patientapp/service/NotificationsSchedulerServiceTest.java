package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseCompletionInformation;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class NotificationsSchedulerServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationsSchedulerService service;

    private final String patientId = String.valueOf(UUID.randomUUID());

    private Patient basePatient(String lang) {
        Patient patient = new Patient();
        patient.setId(String.valueOf(UUID.randomUUID()));
        patient.setGetNotifications(true);
        patient.setLanguage(lang);
        return patient;
    }

    private Exercise setupExerciseForReminder() {
        Exercise exercise = new Exercise();
        exercise.setDoEveryNDays(1);
        exercise.setLastReminderSentAt(Instant.now().minusSeconds(15 * 60 * 60)); // 15h ago

        // Start time so that nextDue is slightly in the future
        ExerciseCompletionInformation info = new ExerciseCompletionInformation();
        info.setStartTime(Instant.now().minusSeconds(86400L - 60)); // Due in 1 min
        exercise.setExerciseCompletionInformation(List.of(info));

        return exercise;
    }

    private PsychologicalTestAssignment setupPsychologicalTestAssignment(Patient patient) {
        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setLastCompletedAt(Instant.now().plusSeconds(3600));
        assignment.setLastReminderSentAt(Instant.now().minusSeconds(14 * 60 * 60));
        assignment.setPatient(patient);
        return assignment;
    }

    @Test
    void testNotifyOnePatient_notificationsDisabled() {
        Patient patient = new Patient();
        patient.setGetNotifications(false);

        service.notifyOnePatient(patient);

        // Expect no interactions with meetingRepository
        Mockito.verifyNoInteractions(meetingRepository);
    }

    @Test
    void testNotifyOnePatient_needsNotificationFalse() {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setGetNotifications(true);

        Meeting meeting = new Meeting();
        meeting.setStartAt(Instant.now().plusSeconds(48 * 60 * 60)); // too far in future
        meeting.setLastReminderSentAt(Instant.now());

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(patientId)).thenReturn(List.of(meeting));

        service.notifyOnePatient(patient);

        // Meeting is fetched, but not saved since needsNotification is false
        verify(meetingRepository).findByPatientIdOrderByStartAtAsc(patientId);
        verify(meetingRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_notificationSent_english() {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setGetNotifications(true);
        patient.setLanguage("en");

        Meeting meeting = new Meeting();
        meeting.setStartAt(Instant.now().plusSeconds(1 * 60 * 60)); // within 24h
        meeting.setLastReminderSentAt(Instant.now().minusSeconds(14 * 60 * 60)); // >13h ago

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(patientId)).thenReturn(List.of(meeting));

        service.notifyOnePatient(patient);

        verify(meetingRepository).save(meeting);
    }

    @Test
    void testNotifyOnePatient_notificationSent_german() {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setGetNotifications(true);
        patient.setLanguage("de");

        Meeting meeting = new Meeting();
        meeting.setStartAt(Instant.now().plusSeconds(2 * 60 * 60));
        meeting.setLastReminderSentAt(Instant.now().minusSeconds(14 * 60 * 60));

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(patientId)).thenReturn(List.of(meeting));

        service.notifyOnePatient(patient);

        verify(meetingRepository).save(meeting);
    }

    @Test
    void testNotifyOnePatient_notificationSent_otherLanguage() {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setGetNotifications(true);
        patient.setLanguage("uk");

        Meeting meeting = new Meeting();
        meeting.setStartAt(Instant.now().plusSeconds(2 * 60 * 60));
        meeting.setLastReminderSentAt(Instant.now().minusSeconds(14 * 60 * 60));

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(patientId)).thenReturn(List.of(meeting));

        service.notifyOnePatient(patient);

        verify(meetingRepository).save(meeting);
    }

    @Test
    void testNotifyOnePatient_exerciseListEmpty() {
        Patient patient = basePatient("en");
        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(List.of());

        service.notifyOnePatient(patient);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_exerciseNeedsNotificationFalse() {
        Patient patient = basePatient("en");

        ExerciseCompletionInformation info = new ExerciseCompletionInformation();
        info.setStartTime(Instant.now().minusSeconds(3 * 86400L)); // long ago

        Exercise exercise = new Exercise();
        exercise.setDoEveryNDays(7);
        exercise.setLastReminderSentAt(Instant.now());
        exercise.setExerciseCompletionInformation(List.of(info));

        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(List.of(exercise));

        service.notifyOnePatient(patient);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_exerciseNotification_english() {
        Patient patient = basePatient("en");
        patient.setGetNotifications(true);

        // Stub meetingRepository to avoid early return
        when(meetingRepository.findByPatientIdOrderByStartAtAsc(any()))
                .thenReturn(Collections.emptyList());

        Exercise exercise = setupExerciseForReminder();
        List<Exercise> exercises = List.of(exercise);
        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(exercises);

        service.notifyOnePatient(patient);
        verify(meetingRepository, never()).save(any());
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void testNotifyOnePatient_exerciseNotification_german() {
        Patient patient = basePatient("de");

        Exercise exercise = setupExerciseForReminder();

        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(List.of(exercise));

        service.notifyOnePatient(patient);

        verify(exerciseRepository).save(exercise);
    }

    @Test
    void testNotifyOnePatient_exerciseNotification_otherLang() {
        Patient patient = basePatient("uk");

        Exercise exercise = setupExerciseForReminder();

        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(List.of(exercise));

        service.notifyOnePatient(patient);

        verify(exerciseRepository).save(exercise);
    }

    @Test
    void testNotifyOnePatient_noTestAssignments() {
        Patient patient = basePatient("en");
        patient.setGetNotifications(true);

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(any())).thenReturn(Collections.emptyList());
        when(exerciseRepository.getExercisesByPatientId(any())).thenReturn(Collections.emptyList());
        when(psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(any()))
                .thenReturn(Collections.emptyList());

        service.notifyOnePatient(patient);

        verify(psychologicalTestsAssginmentRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_testAssignmentNoReminderNeeded() {
        Patient patient = basePatient("en");
        patient.setGetNotifications(true);

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setLastCompletedAt(Instant.now());
        assignment.setLastReminderSentAt(Instant.now());

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(any())).thenReturn(Collections.emptyList());
        when(exerciseRepository.getExercisesByPatientId(any())).thenReturn(Collections.emptyList());
        when(psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(any()))
                .thenReturn(List.of(assignment));

        service.notifyOnePatient(patient);

        verify(psychologicalTestsAssginmentRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_testAssignmentReminderEnglish() {
        Patient patient = basePatient("en");
        patient.setGetNotifications(true);

        PsychologicalTestAssignment assignment = setupPsychologicalTestAssignment(patient);
        when(meetingRepository.findByPatientIdOrderByStartAtAsc(anyString())).thenReturn(Collections.emptyList());
        when(exerciseRepository.getExercisesByPatientId(anyString())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(anyString()))
                .thenReturn(Collections.emptyList());
        when(psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(anyString()))
                .thenReturn(List.of(assignment));

        service.notifyOnePatient(patient);

        verify(psychologicalTestsAssginmentRepository).save(assignment);
    }

    @Test
    void testNotifyOnePatient_testAssignmentReminderGerman() {
        Patient patient = basePatient("de");
        patient.setGetNotifications(true);

        PsychologicalTestAssignment assignment = setupPsychologicalTestAssignment(patient);

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(any())).thenReturn(Collections.emptyList());
        when(exerciseRepository.getExercisesByPatientId(any())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(any())).thenReturn(Collections.emptyList());
        when(psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(anyString()))
                .thenReturn(List.of(assignment));

        service.notifyOnePatient(patient);

        verify(psychologicalTestsAssginmentRepository).save(assignment);
    }

    @Test
    void testNotifyOnePatient_testAssignmentReminderFallbackLanguage() {
        Patient patient = basePatient("uk");
        patient.setGetNotifications(true);

        PsychologicalTestAssignment assignment = setupPsychologicalTestAssignment(patient);

        when(meetingRepository.findByPatientIdOrderByStartAtAsc(any())).thenReturn(Collections.emptyList());
        when(exerciseRepository.getExercisesByPatientId(any())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(any())).thenReturn(Collections.emptyList());
        when(psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(anyString()))
                .thenReturn(List.of(assignment));

        service.notifyOnePatient(patient);

        verify(psychologicalTestsAssginmentRepository).save(assignment);
    }

    @Test
    void testNotifyOnePatient_noJournalEntries() {
        Patient patient = basePatient("en");

        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()))
                .thenReturn(Collections.emptyList());

        service.notifyOnePatient(patient);

        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_journalEntry_noReminderNeeded() {
        Patient patient = basePatient("en");

        JournalEntry entry = new JournalEntry();
        entry.setUpdatedAt(Instant.now().plus(1, ChronoUnit.DAYS)); // not in past
        entry.setLastReminderSentAt(Instant.now()); // just sent

        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()))
                .thenReturn(List.of(entry));

        service.notifyOnePatient(patient);

        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void testNotifyOnePatient_journalEntry_reminderNeeded_en() {
        Patient patient = basePatient("en");
        Instant now = Instant.now();

        JournalEntry entry = new JournalEntry();
        entry.setUpdatedAt(now.minus(1, ChronoUnit.DAYS)); // 1 day ago
        entry.setLastReminderSentAt(Instant.now().minus(15, ChronoUnit.HOURS));

        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()))
                .thenReturn(List.of(entry));

        service.notifyOnePatient(patient);

        verify(journalEntryRepository).save(entry);
    }

    @Test
    void testNotifyOnePatient_journalEntry_reminderNeeded_de() {
        Patient patient = basePatient("de");

        Instant now = Instant.now();

        JournalEntry entry = new JournalEntry();
        entry.setUpdatedAt(now.minus(1, ChronoUnit.DAYS)); // 1 day ago
        entry.setLastReminderSentAt(now.minus(15, ChronoUnit.HOURS)); // 15h ago

        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()))
                .thenReturn(List.of(entry));

        service.notifyOnePatient(patient);

        verify(journalEntryRepository).save(entry);
    }

    @Test
    void testNotifyOnePatient_journalEntry_reminderNeeded_otherLang() {
        Patient patient = basePatient("uk");
        Instant now = Instant.now();

        JournalEntry entry = new JournalEntry();
        entry.setUpdatedAt(now.minus(1, ChronoUnit.DAYS)); // 1 day ago
        entry.setLastReminderSentAt(Instant.now().minus(15, ChronoUnit.HOURS));

        when(journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()))
                .thenReturn(List.of(entry));

        service.notifyOnePatient(patient);

        verify(journalEntryRepository).save(entry);
    }

}
