package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class NotificationSceduler implements Runnable{

    private final Thread schedulerThread = new Thread(this);
    private final PatientRepository patientRepository;
    private final ExerciseRepository exerciseRepository;
    private final MeetingRepository meetingRepository;
    private final PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository;
    private final JournalEntryRepository journalEntryRepository;

    public NotificationSceduler(PatientRepository patientRepository, ExerciseRepository exerciseRepository, MeetingRepository meetingRepository,PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository, JournalEntryRepository journalEntryRepository) {
        this.patientRepository = patientRepository;
        this.meetingRepository = meetingRepository;
        this.exerciseRepository = exerciseRepository;
        this.psychologicalTestsAssginmentRepository = psychologicalTestsAssginmentRepository;
        this.journalEntryRepository = journalEntryRepository;
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
                    Meeting meeting = meetingRepository.findByPatientIdOrderByStartAtAsc(patient.getId()).getFirst();
                    if (needsNotification(meeting.getStartAt(), meeting.getLastReminderSentAt())) {
                        notificationService.sendReminder(patient);
                        meeting.setLastReminderSentAt(Instant.now());
                        meetingRepository.save(meeting);
                        return;
                    }

                    List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
                    for (Exercise exercise : exercises) {
                        Instant nextDue = exercise.getExerciseCompletionInformation().getFirst().getStartTime()
                                .plusSeconds(exercise.getDoEveryNDays() * 86400L);
                        if (needsNotification(nextDue, exercise.getLastReminderSentAt())) {
                            notificationService.sendReminder(patient);
                            exercise.setLastReminderSentAt(Instant.now());
                            exerciseRepository.save(exercise);
                            return;
                        }
                    }
                    PsychologicalTestAssignment psychologicalTestAssignment = psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(patient.getId()).getFirst();
                    if (needsNotification(psychologicalTestAssignment.getLastCompletedAt(), psychologicalTestAssignment.getLastReminderSentAt())) {
                        notificationService.sendReminder(patient);
                        psychologicalTestAssignment.setLastReminderSentAt(Instant.now());
                        psychologicalTestsAssginmentRepository.save(psychologicalTestAssignment);
                        return;
                    }
                    JournalEntry journalEntry = journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId()).getFirst();
                    if(needsNotification(journalEntry.getUpdatedAt(), journalEntry.getLastReminderSentAt())) {
                        notificationService.sendReminder(patient);
                        journalEntry.setLastReminderSentAt(Instant.now());
                        journalEntryRepository.save(journalEntry);
                        return;
                    }

                }

                Thread.sleep(3 * 60 * 60 * 1000); // Sleep for 4 hours
            } catch (InterruptedException e) {
                log.warn("Notification scheduler interrupted");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in notification scheduler", e);
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
