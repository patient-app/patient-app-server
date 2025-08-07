package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.NotificationType;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private static Stream<Arguments> notificationData() {
        return Stream.of(
                // language, type, expectedLocale, urlSuffix, subjectKeyword, ctaKeyword
                Arguments.of("en", NotificationType.MEETING, Locale.ENGLISH, "/", "Meeting", "See"),
                Arguments.of("de", NotificationType.EXERCISE, Locale.GERMAN, "/exercise", "Übung", "Übung"),
                Arguments.of("uk", NotificationType.QUESTIONNAIRES, Locale.forLanguageTag("uk"), "/questionnaires",
                        "анкета", "Заповнити"),
                Arguments.of("en", NotificationType.JOURNAL, Locale.ENGLISH, "/journal", "write", "Open"));
    }

    @ParameterizedTest
    @MethodSource("notificationData")
    void sendNotification_keywordsRemainPresent(
            String lang,
            NotificationType type,
            Locale expectedLocale,
            String expectedUrlSuffix,
            String subjectKeyword,
            String ctaKeyword) {
        // arrange
        Patient p = new Patient();
        p.setEmail("bob@example.com");
        p.setName("Bob");
        p.setLanguage(lang);

        // act
        notificationService.sendNotification(p, type);

        // capture
        @SuppressWarnings("unchecked")
        ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> msgCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> ctaCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Locale> locCap = ArgumentCaptor.forClass(Locale.class);

        verify(emailService).sendEmailNotification(
                toCap.capture(),
                subjCap.capture(),
                nameCap.capture(),
                msgCap.capture(),
                ctaCap.capture(),
                urlCap.capture(),
                locCap.capture());

        // assertions
        assertEquals("bob@example.com", toCap.getValue());
        assertEquals("Bob", nameCap.getValue());
        assertEquals(expectedLocale, locCap.getValue());

        String subject = subjCap.getValue().toLowerCase();
        assertTrue(subject.contains(subjectKeyword.toLowerCase()),
                () -> "Subject should contain “" + subjectKeyword + "”, was: " + subject);

        String message = msgCap.getValue().toLowerCase();
        assertTrue(message.contains("lumina"),
                () -> "Message should mention 'Lumina', was: " + message);

        String cta = ctaCap.getValue();
        assertTrue(cta.toLowerCase().contains(ctaKeyword.toLowerCase()),
                () -> "CTA should contain “" + ctaKeyword + "”, was: " + cta);

        String url = urlCap.getValue();
        assertTrue(url.endsWith(expectedUrlSuffix),
                () -> "URL should end with “" + expectedUrlSuffix + "”, was: " + url);
    }

    @ParameterizedTest
    @MethodSource("notificationData")
    void sendNotification_nullNameFallsBackToClient(
            String lang,
            NotificationType type,
            Locale expectedLocale,
            String expectedUrlSuffix,
            String subjectKeyword,
            String ctaKeyword) {
        Patient p = new Patient();
        p.setEmail("x@example.com");
        p.setName(null);
        p.setLanguage(lang);

        notificationService.sendNotification(p, type);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<String> nameCap = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmailNotification(
                anyString(), anyString(), nameCap.capture(),
                anyString(), anyString(), anyString(), any());
        assertEquals("Client", nameCap.getValue(),
                "Null patient name should fall back to 'Client'");
    }

}
