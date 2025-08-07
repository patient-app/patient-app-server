package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.NotificationType;
import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class NotificationsSchedulerService implements Runnable {

    private final Thread schedulerThread = new Thread(this);
    private final PatientRepository patientRepository;
    private final ExerciseRepository exerciseRepository;
    private final MeetingRepository meetingRepository;
    private final PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final NotificationService notificationService;
    // private final PushNotificationService pushNotificationService = new
    // PushNotificationService();

    public NotificationsSchedulerService(PatientRepository patientRepository, ExerciseRepository exerciseRepository,
            MeetingRepository meetingRepository,
            PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository,
            JournalEntryRepository journalEntryRepository,
            NotificationService notificationService/* , PushNotificationService pushNotificationService */) {
        this.patientRepository = patientRepository;
        this.meetingRepository = meetingRepository;
        this.exerciseRepository = exerciseRepository;
        this.psychologicalTestsAssginmentRepository = psychologicalTestsAssginmentRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.notificationService = notificationService;
        // this.pushNotificationService = pushNotificationService;
    }

    @PostConstruct
    public void startThread() {
        schedulerThread.setDaemon(true);
        schedulerThread.setName("notification-scheduler");
        schedulerThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                log.info("⏰ Notification scheduler started at {}", Instant.now());

                List<Patient> patients = patientRepository.findAll();
                for (Patient patient : patients) {
                    try {
                        notifyOnePatient(patient);
                    } catch (Exception e) {
                        log.error("Error notifying patient {}", patient.getId(), e);
                    }
                }

            } catch (Exception e) {
                log.error("Unexpected error in notification scheduler", e);
            }

            try {
                // TODO:
                // Thread.sleep(3 * 60 * 60 * 1000); // Sleep for 3 hours
                Thread.sleep(5 * 60 * 1000); // Sleep for 5min for testing
            } catch (InterruptedException e) {
                log.warn("Notification scheduler interrupted");
                break;
            }
        }
    }

    public void notifyOnePatient(Patient patient) {
        if (!patient.isGetNotifications()) {
            return;
        }
        List<Meeting> meetings = meetingRepository.findByPatientIdOrderByStartAtAsc(patient.getId());
        if (!meetings.isEmpty()) {

            Meeting meeting = meetings.get(0);
            if (needsNotification(meeting.getStartAt(), meeting.getLastReminderSentAt())) {
                notificationService.sendNotification(patient, NotificationType.MEETING);

                meeting.setLastReminderSentAt(Instant.now());
                meetingRepository.save(meeting);
                return;
            }
        }

        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        // TODO: order Liste by strat time such that first is latest start time or
        // instead of get first geet wher start time is max.
        for (Exercise exercise : exercises) {
            Instant nextDue = exercise.getExerciseCompletionInformation().getFirst().getStartTime()
                    .plusSeconds(exercise.getDoEveryNDays() * 86400L);
            if (needsNotification(nextDue, exercise.getLastReminderSentAt())) {
                notificationService.sendNotification(patient, NotificationType.EXERCISE);

                exercise.setLastReminderSentAt(Instant.now());
                exerciseRepository.save(exercise);
                return;
            }
        }

        // TODO:check order b
        List<PsychologicalTestAssignment> testAssignments = psychologicalTestsAssginmentRepository
                .findByPatientIdOrderByLastCompletedAtAsc(patient.getId());

        if (!testAssignments.isEmpty()) {
            PsychologicalTestAssignment psychologicalTestAssignment = testAssignments.getFirst();

            Instant nextDue = psychologicalTestAssignment.getLastCompletedAt()
                    .plusSeconds(psychologicalTestAssignment.getDoEveryNDays() * 86400L);

            if (needsNotification(nextDue, psychologicalTestAssignment.getLastReminderSentAt())) {
                notificationService.sendNotification(patient, NotificationType.QUESTIONNAIRES);

                psychologicalTestAssignment.setLastReminderSentAt(Instant.now());
                psychologicalTestsAssginmentRepository.save(psychologicalTestAssignment);
                return;
            }
        }

        // TODO: check order by
        List<JournalEntry> journalEntries = journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId());
        if (!journalEntries.isEmpty()) {

            JournalEntry journalEntry = journalEntries.getFirst();

            Instant nextDue = journalEntry.getUpdatedAt()
                    .plus(2, ChronoUnit.DAYS);
            if (needsNotification(nextDue, journalEntry.getLastReminderSentAt())) {
                notificationService.sendNotification(patient, NotificationType.JOURNAL);

                journalEntry.setLastReminderSentAt(Instant.now());
                journalEntryRepository.save(journalEntry);
            }
        }
    }

    private boolean needsNotification(Instant timestamp, Instant lastReminderSentAt) {
        Instant now = Instant.now();
        Instant twentyFourHoursLater = now.plusSeconds(24 * 60 * 60);

        return timestamp.isAfter(now) &&
                timestamp.isBefore(twentyFourHoursLater) &&
                (lastReminderSentAt == null || lastReminderSentAt.isBefore(now.minusSeconds(13 * 60 * 60)));
    }

}
