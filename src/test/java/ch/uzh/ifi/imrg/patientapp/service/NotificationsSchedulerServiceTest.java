package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseCompletionInformation;
import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class NotificationsSchedulerServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private MeetingRepository meetingRepository;
    @Mock
    private PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository;
    @Mock private JournalEntryRepository journalEntryRepository;

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
        exercise.setLastReminderSentAt(Instant.now().minusSeconds(15 * 60 * 60)); // >13h ago

        ExerciseCompletionInformation info = new ExerciseCompletionInformation();
        info.setStartTime(Instant.now().minusSeconds(2 * 86400L)); // last done 2 days ago
        exercise.setExerciseCompletionInformation(List.of(info));

        return exercise;
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

        Exercise exercise = setupExerciseForReminder();

        when(exerciseRepository.getExercisesByPatientId(patient.getId()))
                .thenReturn(List.of(exercise));

        service.notifyOnePatient(patient);

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




}

