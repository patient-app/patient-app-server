package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariables {

  private static String jwtSecretKey;

  @Autowired
  public EnvironmentVariables(@Value("${JWT_SECRET_KEY}") String jwtSecretKey) {
    EnvironmentVariables.jwtSecretKey = jwtSecretKey;
  }

  public static String getJwtSecretKey() {
    return jwtSecretKey;
  }
}
