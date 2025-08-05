package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.*;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class NotificationsSchedulerService implements Runnable{

    private final Thread schedulerThread = new Thread(this);
    private final PatientRepository patientRepository;
    private final ExerciseRepository exerciseRepository;
    private final MeetingRepository meetingRepository;
    private final PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository;
    private final JournalEntryRepository journalEntryRepository;
    //private final PushNotificationService pushNotificationService = new PushNotificationService();

    public NotificationsSchedulerService(PatientRepository patientRepository, ExerciseRepository exerciseRepository, MeetingRepository meetingRepository, PsychologicalTestsAssignmentRepository psychologicalTestsAssginmentRepository, JournalEntryRepository journalEntryRepository/*, PushNotificationService pushNotificationService*/) {
        this.patientRepository = patientRepository;
        this.meetingRepository = meetingRepository;
        this.exerciseRepository = exerciseRepository;
        this.psychologicalTestsAssginmentRepository = psychologicalTestsAssginmentRepository;
        this.journalEntryRepository = journalEntryRepository;
        //this.pushNotificationService = pushNotificationService;
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
                Thread.sleep(3 * 60 * 60 * 1000); // Sleep for 3 hours
            } catch (InterruptedException e) {
                log.warn("Notification scheduler interrupted");
                break;
            }
        }
    }



    public void notifyOnePatient(Patient patient) {
        if (!patient.isGetNotifications()){
            return;
        }
        List<Meeting> meetings = meetingRepository.findByPatientIdOrderByStartAtAsc(patient.getId());
        if (!meetings.isEmpty()) {

            Meeting meeting = meetings.get(0);
            if (needsNotification(meeting.getStartAt(), meeting.getLastReminderSentAt())) {
                if (patient.getLanguage().equals("en")) {
                    //pushNotificationService.sendToPatient(patient.getId(),"Lumina","You have a meeting scheduled soon. Please check your app for details.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");
                } else if (patient.getLanguage().equals("de")) {
                    //pushNotificationService.sendToPatient(patient.getId(),"Lumina","Du hast bald ein Meeting. Bitte schau in der App nach.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                } else {
                    //pushNotificationService.sendToPatient(patient.getId(),"Lumina","У вас незабаром запланована зустріч. Будь ласка, перевірте додаток для отримання деталей.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                }
                meeting.setLastReminderSentAt(Instant.now());
                meetingRepository.save(meeting);
                return;
            }
        }

        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        for (Exercise exercise : exercises) {
            Instant nextDue = exercise.getExerciseCompletionInformation().getFirst().getStartTime()
                    .plusSeconds(exercise.getDoEveryNDays() * 86400L);
            if (needsNotification(nextDue, exercise.getLastReminderSentAt())) {
                if(patient.getLanguage().equals("en")){
                    // pushNotificationService.sendToPatient(patient.getId(), "Lumina", "There is a new exercise to do.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                }else if (patient.getLanguage().equals("de")){
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Es gibt eine neue Übung zu machen.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                } else {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "З’явилася нова вправа для виконання.");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                }
                exercise.setLastReminderSentAt(Instant.now());
                exerciseRepository.save(exercise);
                return;
            }
        }
        List<PsychologicalTestAssignment> testAssignments =
                psychologicalTestsAssginmentRepository.findByPatientIdOrderByLastCompletedAtAsc(patient.getId());
        System.out.println("what");
        if (!testAssignments.isEmpty()) {
            System.out.println("testAssignments = " + testAssignments);
            PsychologicalTestAssignment psychologicalTestAssignment = testAssignments.getFirst();

            Instant nextDue = psychologicalTestAssignment.getLastCompletedAt()
                    .plusSeconds(psychologicalTestAssignment.getDoEveryNDays() * 86400L);

            if (needsNotification(nextDue, psychologicalTestAssignment.getLastReminderSentAt())) {
                System.out.println("hi");
                if (patient.getLanguage().equals("en")) {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Want to do a test?");
                    System.out.println("You have a assignment scheduled soon. Please check your app for details.");

                } else if (patient.getLanguage().equals("de")) {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Möchtest du einen Test machen?");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                } else {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Хочете пройти тест?");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                }
                psychologicalTestAssignment.setLastReminderSentAt(Instant.now());
                psychologicalTestsAssginmentRepository.save(psychologicalTestAssignment);
                return;
            }
        }

        List<JournalEntry> journalEntries =
                journalEntryRepository.findByPatientIdOrderByUpdatedAtAsc(patient.getId());
        if (!journalEntries.isEmpty()) {

            JournalEntry journalEntry = journalEntries.getFirst();
            System.out.println("journalEntry = " + journalEntry);

            Instant nextDue = journalEntry.getUpdatedAt()
                    .plus(2,ChronoUnit.DAYS);
            if (needsNotification(nextDue, journalEntry.getLastReminderSentAt())) {
                if (patient.getLanguage().equals("en")) {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "You haven't journaled in a while. Why don't you give it a try?");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                } else if (patient.getLanguage().equals("de")) {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Du hast schon lange nicht mehr Tagebuch geschrieben. Warum versuchst du es nicht mal wieder?");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                } else {
                    //pushNotificationService.sendToPatient(patient.getId(), "Lumina", "Ви давно не вели щоденник. Чому б не спробувати зараз?");
                    System.out.println("You have a meeting scheduled soon. Please check your app for details.");

                }
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
