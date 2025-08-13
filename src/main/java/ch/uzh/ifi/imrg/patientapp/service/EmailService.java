package ch.uzh.ifi.imrg.patientapp.service;

import java.util.Locale;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        sendEmail(to, subject, text, false);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent, true);
    }

    private void sendEmail(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordReset(String to, String clientName, String newPassword, Locale locale) {

        String lang = locale.getLanguage().toLowerCase();
        String templateName = "password-reset_" + lang;
        String subject = switch (lang) {
            case "de" -> "Lumina – Ihr neues Passwort";
            case "uk" -> "Lumina — Ваш новий пароль";
            default -> "Lumina — Your new password";
        };

        Context ctx = new Context(locale);
        ctx.setVariable("clientName", clientName);
        ctx.setVariable("newPassword", newPassword);
        ctx.setVariable("loginUrl", "https://vllm-imrg.ifi.uzh.ch/client/login");
        ctx.setVariable("supportEmail", "lumina.ifi@gmail.com");

        String htmlContent = templateEngine.process(templateName, ctx);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendEmailNotification(String to, String notificationSubject, String clientName,
            String notificationMessage, String ctaText,
            String appUrl, Locale locale) {

        String lang = locale.getLanguage().toLowerCase();
        String templateName = "notification-email_" + lang;

        Context ctx = new Context(locale);
        ctx.setVariable("notificationSubject", notificationSubject);
        ctx.setVariable("clientName", clientName);
        ctx.setVariable("notificationMessage", notificationMessage);
        ctx.setVariable("ctaText", ctaText);
        ctx.setVariable("appUrl", appUrl);
        ctx.setVariable("supportEmail", "lumina.ifi@gmail.com");

        String htmlContent = templateEngine.process(templateName, ctx);

        sendHtmlEmail(to, notificationSubject, htmlContent);
    }

}
