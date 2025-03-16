package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

  private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

  private PasswordUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String encryptPassword(String rawPassword) {
    return PASSWORD_ENCODER.encode(rawPassword);
  }

  public static boolean checkPassword(String rawPassword, String encryptedPassword) {
    return PASSWORD_ENCODER.matches(rawPassword, encryptedPassword);
  }
}
