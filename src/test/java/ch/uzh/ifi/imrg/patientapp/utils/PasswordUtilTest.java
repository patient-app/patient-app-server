package ch.uzh.ifi.imrg.patientapp.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;

import java.util.regex.Pattern;

import static ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil.Alphabet.CYRILLIC;
import static ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil.Alphabet.LATIN;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordUtilTest {

    private static final Pattern LATIN_PATTERN = Pattern.compile("^[A-Za-z0-9]{4}(?:-[A-Za-z0-9]{4}){3}$");
    private static final Pattern CYRILLIC_PATTERN = Pattern
            .compile("^[\\p{IsCyrillic}0-9]{4}(?:-[\\p{IsCyrillic}0-9]{4}){3}$");

    @Test
    void encryptPassword_shouldReturnHashedPassword() {
        String rawPassword = "mySecret123";
        String encrypted = PasswordUtil.encryptPassword(rawPassword);

        assertNotNull(encrypted);
        assertNotEquals(rawPassword, encrypted);
        assertTrue(PasswordUtil.checkPassword(rawPassword, encrypted));
    }

    @Test
    void checkPassword_shouldReturnTrueForCorrectMatch() {
        String rawPassword = "password";
        String encrypted = PasswordUtil.encryptPassword(rawPassword);

        assertTrue(PasswordUtil.checkPassword(rawPassword, encrypted));
    }

    @Test
    void checkPassword_shouldReturnFalseForIncorrectPassword() {
        String rawPassword = "password";
        String wrongPassword = "wrongpass";
        String encrypted = PasswordUtil.encryptPassword(rawPassword);

        assertFalse(PasswordUtil.checkPassword(wrongPassword, encrypted));
    }

    @Test
    void constructor_shouldThrowException() throws Exception {
        var constructor = PasswordUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected UnsupportedOperationException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            assertEquals("Utility class", e.getCause().getMessage());
        }
    }

    @Test
    void generatePassword_latin_shouldMatchFormatAndContainAllClasses() {
        String pwd = PasswordUtil.generatePassword(LATIN);

        // 1) Format xxxx-xxxx-xxxx-xxxx
        assertTrue(LATIN_PATTERN.matcher(pwd).matches(),
                "Password must be in 4 blocks of 4 alphanumeric chars separated by hyphens");

        // 2) At least one uppercase, one lowercase, one digit
        assertTrue(pwd.chars().anyMatch(Character::isUpperCase),
                "Password must contain at least one uppercase letter");
        assertTrue(pwd.chars().anyMatch(Character::isLowerCase),
                "Password must contain at least one lowercase letter");
        assertTrue(pwd.chars().anyMatch(Character::isDigit),
                "Password must contain at least one digit");
    }

    @Test
    void generatePassword_cyrillic_shouldMatchFormatAndContainAllClasses() {
        String pwd = PasswordUtil.generatePassword(CYRILLIC);

        // 1) Format xxxx-xxxx-xxxx-xxxx (Cyrillic letters or digits)
        assertTrue(CYRILLIC_PATTERN.matcher(pwd).matches(),
                "Password must be in 4 blocks of 4 Cyrillic letters or digits separated by hyphens");

        // 2) At least one uppercase Cyrillic, one lowercase Cyrillic, one digit
        boolean hasUpper = pwd.codePoints()
                .anyMatch(cp -> Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.CYRILLIC
                        && Character.isUpperCase(cp));
        boolean hasLower = pwd.codePoints()
                .anyMatch(cp -> Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.CYRILLIC
                        && Character.isLowerCase(cp));
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);

        assertTrue(hasUpper, "Password must contain at least one uppercase Cyrillic letter");
        assertTrue(hasLower, "Password must contain at least one lowercase Cyrillic letter");
        assertTrue(hasDigit, "Password must contain at least one digit");
    }

}
