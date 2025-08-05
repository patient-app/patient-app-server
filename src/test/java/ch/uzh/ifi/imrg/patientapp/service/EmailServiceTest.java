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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

        @Mock
        private JavaMailSender mailSender;

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
}
