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
                            "Erinnerung: Sitzung",
                            "Bald findet Ihre nächste Sitzung statt. Die Uhrzeit und Details finden Sie in der Lumina-App.",
                            "Sitzung ansehen",
                            baseUrl + "/");
                    case "uk" -> new NotificationContent(
                            "Нагадування про зустріч",
                            "Незабаром у вас запланована зустріч. Переглянути час та деталі можна в додатку Lumina.",
                            "Переглянути зустріч",
                            baseUrl + "/");
                    default -> new NotificationContent(
                            "Upcoming Meeting",
                            "You’ve got a meeting coming up soon. You can check the time and details anytime in your Lumina app.",
                            "See meeting info",
                            baseUrl + "/");
                };
            }
            case EXERCISE -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Zeit für Ihre Übung",
                            "In Ihrer Lumina-App wartet eine Übung auf Sie. Öffnen Sie die App, wenn Sie bereit sind, loszulegen.",
                            "Übung starten",
                            baseUrl + "/exercise");
                    case "uk" -> new NotificationContent(
                            "Час для вправи",
                            "У додатку Lumina на вас чекає вправа. Відкрийте додаток, коли будете готові почати.",
                            "Виконати вправу",
                            baseUrl + "/exercise");
                    default -> new NotificationContent(
                            "Time for your exercise",
                            "You’ve got an exercise waiting for you in Lumina. Open the app whenever you’re ready to give it a go.",
                            "Do the exercise",
                            baseUrl + "/exercise");
                };
            }
            case QUESTIONNAIRES -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Fragebogen fällig",
                            "Ein Fragebogen wartet in Ihrer Lumina-App. Es dauert nur einen Moment – füllen Sie ihn aus, wenn es passt.",
                            "Ausfüllen",
                            baseUrl + "/questionnaires");
                    case "uk" -> new NotificationContent(
                            "Анкета чекає",
                            "У додатку Lumina доступна анкета. Це займе всього кілька хвилин — заповніть, коли буде зручно.",
                            "Заповнити анкету",
                            baseUrl + "/questionnaires");
                    default -> new NotificationContent(
                            "Questionnaire due",
                            "One of your questionnaires is ready in the Lumina app. It only takes a moment — complete it when you’re ready.",
                            "Fill it out",
                            baseUrl + "/questionnaires");
                };
            }
            case JOURNAL -> {
                return switch (locale.getLanguage()) {
                    case "de" -> new NotificationContent(
                            "Möchten Sie etwas festhalten?",
                            "Ihr Journal ist jederzeit in der Lumina-App für Sie da. Nehmen Sie sich ein paar Minuten zum Reflektieren oder einfach zum Schreiben.",
                            "Journal öffnen",
                            baseUrl + "/journal");
                    case "uk" -> new NotificationContent(
                            "Хочете щось записати?",
                            "Ваш журнал завжди поруч у додатку Lumina. Приділіть кілька хвилин, щоб подумати, виразитись або просто заспокоїтись.",
                            "Відкрити щоденник",
                            baseUrl + "/journal");
                    default -> new NotificationContent(
                            "Want to write something down?",
                            "Your journal is always there for you in the Lumina app. Take a few minutes to reflect, express, or just clear your mind.",
                            "Open journal",
                            baseUrl + "/journal");
                };
            }
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }

}
