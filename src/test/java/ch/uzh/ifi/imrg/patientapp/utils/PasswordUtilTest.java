package ch.uzh.ifi.imrg.patientapp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PasswordUtilTest {
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
    void constructor_shouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            // Use reflection to try to call the private constructor
            java.lang.reflect.Constructor<PasswordUtil> constructor = PasswordUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }
}
