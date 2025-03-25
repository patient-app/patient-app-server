package ch.uzh.ifi.imrg.patientapp.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptographyUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private CryptographyUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    private static SecretKeySpec getSecretKeyFromBase64(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, "AES");
    }
    public static String encrypt(String plaintext) {
        try {
            String base64Key = EnvironmentVariables.getJwtSecretKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
            // make a random initialization vector
            byte[] ivBytes = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            //prepends the IV to the encrypted message
            byte[] ivPlusCipher = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, ivPlusCipher, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, ivPlusCipher, ivBytes.length, encrypted.length);
            return Base64.getEncoder().encodeToString(ivPlusCipher);        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String base64Encrypted) {
        try {
            String base64Key = EnvironmentVariables.getJwtSecretKey();
            byte[] ivPlusCipher = Base64.getDecoder().decode(base64Encrypted);

            // Extract IV and ciphertext
            byte[] ivBytes = new byte[16];
            byte[] cipherBytes = new byte[ivPlusCipher.length - 16];
            System.arraycopy(ivPlusCipher, 0, ivBytes, 0, 16);
            System.arraycopy(ivPlusCipher, 16, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decrypted = cipher.doFinal(cipherBytes);

            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
