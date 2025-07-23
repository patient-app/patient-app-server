package ch.uzh.ifi.imrg.patientapp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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

    @Test
    void sendSimpleMessage_shouldPopulateAndSendMessage() {
        // Act
        emailService.sendSimpleMessage(to, subject, text);

        // Assert: mailSender.send(...) was called once with a SimpleMailMessage
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[] { to }, msg.getTo(),
                "Recipient address should match");
        assertEquals(subject, msg.getSubject(),
                "Subject should match");
        assertEquals(text, msg.getText(),
                "Body text should match");
    }

    @Test
    void sendSimpleMessage_whenMailSenderThrows_shouldPropagateException() {
        // Arrange: simulate an error in the mail sender
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert: exception bubbles up
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendSimpleMessage(to, subject, text));
        assertEquals("SMTP error", ex.getMessage());
    }
}
