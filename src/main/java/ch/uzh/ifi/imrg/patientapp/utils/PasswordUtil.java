package ch.uzh.ifi.imrg.patientapp.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

  public enum Alphabet {
    LATIN, CYRILLIC
  }

  private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

  private static final SecureRandom RANDOM = new SecureRandom();

  // Latin
  private static final String LATIN_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LATIN_LOWER = "abcdefghijklmnopqrstuvwxyz";

  // Ukrainian/Cyrillic
  private static final String CYRILLIC_UPPER = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";
  private static final String CYRILLIC_LOWER = "абвгґдеєжзииіїйклмнопрстуфхцчшщьюя";

  private static final String DIGITS = "0123456789";

  private PasswordUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String encryptPassword(String rawPassword) {
    return PASSWORD_ENCODER.encode(rawPassword);
  }

  public static boolean checkPassword(String rawPassword, String encryptedPassword) {
    return PASSWORD_ENCODER.matches(rawPassword, encryptedPassword);
  }

  public static String generatePassword(Alphabet alphabet) {

    final String upper = (alphabet == Alphabet.CYRILLIC ? CYRILLIC_UPPER : LATIN_UPPER);
    final String lower = (alphabet == Alphabet.CYRILLIC ? CYRILLIC_LOWER : LATIN_LOWER);
    final String all = upper + lower + DIGITS;

    final int TOTAL = 16;

    List<Character> chars = new ArrayList<>(TOTAL);
    chars.add(randomChar(upper));
    chars.add(randomChar(lower));
    chars.add(randomChar(DIGITS));

    for (int i = 3; i < TOTAL; i++) {
      chars.add(randomChar(all));
    }

    Collections.shuffle(chars, RANDOM);

    StringBuilder sb = new StringBuilder(TOTAL + 3);
    for (int i = 0; i < TOTAL; i++) {
      sb.append(chars.get(i));
      if ((i + 1) % 4 == 0 && i != TOTAL - 1) {
        sb.append('-');
      }
    }
    return sb.toString();
  }

  private static char randomChar(String source) {
    return source.charAt(RANDOM.nextInt(source.length()));
  }
}
