package ch.uzh.ifi.imrg.patientapp.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import javax.crypto.KeyGenerator;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class CryptographyUtilTest {
    @Test
    void encryptAndDecrypt_withKey_shouldReturnOriginal() {
        String key = CryptographyUtil.generatePrivateKey();
        String original = "secret message";

        String encrypted = CryptographyUtil.encrypt(original, key);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = CryptographyUtil.decrypt(encrypted, key);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptAndDecrypt_withEnvKey_shouldReturnOriginal() {
        String fakeKey = "12345678901234567890123456789012"; // 32 chars
        String original = "env secret";

        try (MockedStatic<EnvironmentVariables> mock = mockStatic(EnvironmentVariables.class)) {
            mock.when(EnvironmentVariables::getJwtSecretKey).thenReturn(fakeKey);

            String encrypted = CryptographyUtil.encrypt(original);
            assertNotNull(encrypted);
            assertNotEquals(original, encrypted);

            String decrypted = CryptographyUtil.decrypt(encrypted);
            assertEquals(original, decrypted);
        }
    }

    @Test
    void generatePrivateKey_shouldReturnBase64String() {
        String key = CryptographyUtil.generatePrivateKey();
        assertNotNull(key);
        byte[] decoded = Base64.getDecoder().decode(key);
        assertEquals(32, decoded.length); // 256 bits = 32 bytes
    }
    @Test
    void encrypt_shouldThrowRuntimeException_whenInvalidKey() {
        String plaintext = "Test";
        String invalidBase64Key = "%%%INVALID_BASE64%%%";

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                CryptographyUtil.encrypt(plaintext, invalidBase64Key)
        );

        assertEquals("Encryption failed", ex.getMessage());
    }


    @Test
    void decrypt_shouldThrowRuntimeException_whenDecryptionFails() {
        String encrypted = CryptographyUtil.encrypt("secret", CryptographyUtil.generatePrivateKey());
        String wrongKey = CryptographyUtil.generatePrivateKey(); // different key

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                CryptographyUtil.decrypt(encrypted, wrongKey)
        );

        assertEquals("Decryption failed", ex.getMessage());
    }


    @Test
    void constructor_shouldThrowException() {
        Exception exception = assertThrows(Exception.class, () -> {
            var ctor = CryptographyUtil.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        });

        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class", exception.getCause().getMessage());
    }

}
