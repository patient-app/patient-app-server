package ch.uzh.ifi.imrg.patientapp.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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

}
