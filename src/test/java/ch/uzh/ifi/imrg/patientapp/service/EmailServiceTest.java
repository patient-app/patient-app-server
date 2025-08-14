package ch.uzh.ifi.imrg.patientapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

        @Mock
        private JavaMailSender mailSender;

        @Mock
        private SpringTemplateEngine templateEngine;

        @InjectMocks
        private EmailService emailService;

        private final String to = "user@example.com";
        private final String subject = "Test Subject";
        private final String text = "Hello, this is a test.";
        private final String html = "<h1>HTML Content</h1>";

        @BeforeEach
        void setUp() {
                when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        }

        @Test
        void sendSimpleMessage_shouldPopulateAndSendMimeMessage() throws Exception {
                // Act
                emailService.sendSimpleMessage(to, subject, text);

                // Assert
                ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
                verify(mailSender, times(1)).send(captor.capture());

                MimeMessage msg = captor.getValue();
                Address[] recipients = msg.getRecipients(MimeMessage.RecipientType.TO);
                String[] actualRecipients = Arrays.stream(recipients)
                                .map(Address::toString)
                                .toArray(String[]::new);
                assertArrayEquals(new String[] { to }, actualRecipients, "Recipient address should match");
                assertEquals(subject, msg.getSubject(), "Subject should match");
                assertEquals(text, msg.getContent().toString().trim(), "Body text should match");
                assertTrue(msg.isMimeType("text/plain"), "Should be plain-text");
        }

        @Test
        void sendSimpleMessage_whenMailSenderThrows_shouldPropagateException() {
                doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));
                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> emailService.sendSimpleMessage(to, subject, text));
                assertEquals("SMTP error", ex.getMessage());
        }

        @Test
        void sendHtmlEmail_shouldPopulateAndSendMimeMessage() throws Exception {
                // Act
                emailService.sendHtmlEmail(to, subject, html);

                // Assert
                ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
                verify(mailSender, times(1)).send(captor.capture());

                MimeMessage msg = captor.getValue();
                Address[] recipients = msg.getRecipients(MimeMessage.RecipientType.TO);
                String[] actualRecipients = Arrays.stream(recipients)
                                .map(Address::toString)
                                .toArray(String[]::new);
                assertArrayEquals(new String[] { to }, actualRecipients, "Recipient should match");
                assertEquals(subject, msg.getSubject(), "Subject should match");
                assertTrue(msg.getContent().toString().contains(html), "Content should contain HTML");
        }

        @Test
        void sendHtmlEmail_whenMailSenderThrows_shouldPropagateException() throws Exception {
                MimeMessage mime = new MimeMessage((Session) null);
                when(mailSender.createMimeMessage()).thenReturn(mime);
                doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> emailService.sendHtmlEmail(to, subject, html));
                assertEquals("SMTP error", ex.getMessage());
        }

        @Test
        void sendEmailNotification_shouldSendProcessedHtmlMessage() throws Exception {
                // Arrange
                // 1) stub Thymeleaf
                Locale locale = Locale.ENGLISH;
                String lang = locale.getLanguage().toLowerCase();
                String templateName = "notification-email_" + lang;
                String processedHtml = "<p>Hello %s!</p>".formatted("ClientXYZ");
                when(templateEngine.process(eq(templateName), any(Context.class)))
                                .thenReturn(processedHtml);

                // 2) stub mailSender
                MimeMessage mime = new MimeMessage((Session) null);
                when(mailSender.createMimeMessage()).thenReturn(mime);

                String to = "user@example.com";
                String notificationSubject = "Alert!";
                String clientName = "ClientXYZ";
                String notificationMessage = "You have a new alert.";
                String ctaText = "Click here";
                String appUrl = "https://app.example.com";

                // Act
                emailService.sendEmailNotification(
                                to,
                                notificationSubject,
                                clientName,
                                notificationMessage,
                                ctaText,
                                appUrl,
                                locale);

                // Assert #1: Thymeleaf was invoked
                ArgumentCaptor<Context> ctxCap = ArgumentCaptor.forClass(Context.class);
                verify(templateEngine, times(1))
                                .process(eq(templateName), ctxCap.capture());
                Context ctx = ctxCap.getValue();
                // you can spot-check one variable; if you want all, repeat these asserts:
                assertEquals("ClientXYZ", ctx.getVariable("clientName"));
                assertEquals("lumina.ifi@gmail.com", ctx.getVariable("supportEmail"));

                // Assert #2: mailSender.send(...) got the right MimeMessage
                ArgumentCaptor<MimeMessage> msgCap = ArgumentCaptor.forClass(MimeMessage.class);
                verify(mailSender, times(1)).send(msgCap.capture());

                MimeMessage sent = msgCap.getValue();
                // recipient
                Address[] tos = sent.getRecipients(MimeMessage.RecipientType.TO);
                assertArrayEquals(new String[] { to },
                                Arrays.stream(tos).map(Address::toString).toArray(String[]::new));
                // subject
                assertEquals(notificationSubject, sent.getSubject());

                // content and mime type
                String body = sent.getContent().toString();
                assertTrue(body.contains(processedHtml), "Body should contain the processed HTML");
        }
}
