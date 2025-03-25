package ch.uzh.ifi.imrg.patientapp.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptographyUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private CryptographyUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    private static SecretKeySpec getSecretKeyFromBase64(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, "AES");
    }
    public static String encrypt(String plaintext, String base64Key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
            // make a random initialization vector
            byte[] ivBytes = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, ivBytes);

            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            //prepends the IV to the encrypted message
            byte[] ivPlusCipher = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, ivPlusCipher, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, ivPlusCipher, ivBytes.length, encrypted.length);

            return Base64.getEncoder().encodeToString(ivPlusCipher);        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    public static String encrypt(String plaintext){
        return encrypt(plaintext, EnvironmentVariables.getJwtSecretKey());
    }

    public static String decrypt(String base64Encrypted, String base64Key) {
        try {
            byte[] ivPlusCipher = Base64.getDecoder().decode(base64Encrypted);

            // Extract IV and ciphertext
            byte[] ivBytes = new byte[IV_SIZE];
            byte[] cipherBytes = new byte[ivPlusCipher.length - IV_SIZE];
            System.arraycopy(ivPlusCipher, 0, ivBytes, 0, IV_SIZE);
            System.arraycopy(ivPlusCipher, IV_SIZE, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = getSecretKeyFromBase64(base64Key);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            byte[] decrypted = cipher.doFinal(cipherBytes);

            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
    public static String decrypt(String base64Encrypted){
        return decrypt(base64Encrypted, EnvironmentVariables.getJwtSecretKey());
    }
    public static String generatePrivateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom()); // AES-256
            SecretKey secretKey = keyGen.generateKey();
            byte[] keyBytes = secretKey.getEncoded();
            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }
}
