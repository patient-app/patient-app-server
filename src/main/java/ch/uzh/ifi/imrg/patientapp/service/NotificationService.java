package ch.uzh.ifi.imrg.patientapp.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.imrg.patientapp.constant.NotificationType;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;

@Service
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendNotification(Patient toPatient, NotificationType notificationType) {

        String lang = toPatient.getLanguage() != null ? toPatient.getLanguage().toLowerCase() : "en";
        Locale locale = switch (lang) {
            case "de" -> Locale.GERMAN;
            case "uk" -> Locale.forLanguageTag("uk");
            default -> Locale.ENGLISH;
        };

        String clientName = toPatient.getName() != null ? toPatient.getName() : "Client";

        NotificationContent content = getNotificationContent(notificationType, locale);

        emailService.sendEmailNotification(toPatient.getEmail(), content.subject, clientName, content.message,
                content.ctaText, content.appUrl, locale);
    }

    private static class NotificationContent {
        final String subject;
        final String message;
        final String ctaText;
        final String appUrl;

        NotificationContent(String subject, String message, String ctaText, String appUrl) {
            this.subject = subject;
            this.message = message;
            this.ctaText = ctaText;
            this.appUrl = appUrl;
        }
    }

    private NotificationContent getNotificationContent(NotificationType type, Locale locale) {
        String baseUrl = "https://vllm-imrg.ifi.uzh.ch/client";

        switch (type) {
            case MEETING -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Anstehende Sitzung",
                            "Sie haben in Ihrer Lumina-App eine anstehende Sitzung. Bitte öffnen Sie die App, um die Details zu sehen.",
                            "Zur Sitzung",
                            baseUrl + "/");
                    case "uk" -> new NotificationContent(
                            "Наближається сеанс",
                            "У вашому додатку Lumina наближається сеанс. Будь ласка, відкрийте додаток, щоб переглянути деталі.",
                            "До сеансу",
                            baseUrl + "/");
                    default -> new NotificationContent(
                            "Upcoming Session",
                            "You have an upcoming session in your Lumina app. Please open the app to view the details.",
                            "Go to Session",
                            baseUrl + "/");
                };
            }
            case EXERCISE -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Neue Übung verfügbar",
                            "Eine neue Übung wartet in Ihrer Lumina-App auf Sie. Bitte öffnen Sie die App, um zu beginnen.",
                            "Zur Übung",
                            baseUrl + "/exercise");
                    case "uk" -> new NotificationContent(
                            "Нова вправа доступна",
                            "Нова вправа чекає на вас у додатку Lumina. Будь ласка, відкрийте додаток, щоб розпочати.",
                            "До вправи",
                            baseUrl + "/exercise");
                    default -> new NotificationContent(
                            "New Exercise Available",
                            "A new exercise is waiting in your Lumina app. Please open the app to get started.",
                            "Go to Exercise",
                            baseUrl + "/exercise");
                };
            }
            case QUESTIONNAIRES -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Neuer Fragebogen",
                            "Ein neuer Fragebogen wurde in Ihrer Lumina-App für Sie bereitgestellt. Bitte öffnen Sie die App, um ihn auszufüllen.",
                            "Zum Fragebogen",
                            baseUrl + "/questionnaires");
                    case "uk" -> new NotificationContent(
                            "Новий опитувальник",
                            "У вашому додатку Lumina призначено новий опитувальник. Будь ласка, відкрийте додаток, щоб заповнити його.",
                            "До опитувальника",
                            baseUrl + "/questionnaires");
                    default -> new NotificationContent(
                            "New Questionnaire",
                            "A new questionnaire has been assigned in your Lumina app. Please open the app to complete it.",
                            "Go to Questionnaire",
                            baseUrl + "/questionnaires");
                };
            }
            case JOURNAL -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Journaling-Erinnerung",
                            "Ihre Journaling-Aufgabe wartet in der Lumina-App. Bitte öffnen Sie die App, um zu schreiben.",
                            "Zum Journal",
                            baseUrl + "/journal");
                    case "uk" -> new NotificationContent(
                            "Нагадування про щоденник",
                            "Ваше завдання з ведення щоденника чекає в додатку Lumina. Будь ласка, відкрийте додаток, щоб писати.",
                            "До щоденника",
                            baseUrl + "/journal");
                    default -> new NotificationContent(
                            "Journaling Reminder",
                            "Your journaling task is waiting in the Lumina app. Please open the app to write.",
                            "Go to Journal",
                            baseUrl + "/journal");
                };
            }
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }

}
