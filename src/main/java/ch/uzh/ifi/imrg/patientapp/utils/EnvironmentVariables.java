package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EnvironmentVariables {

  private static String jwtSecretKey;
  private static String chatGptApiKey;


  @Autowired
  public EnvironmentVariables(@Value("${JWT_SECRET_KEY}") String jwtSecretKey,
                              @Value("${CHATGPT_API_KEY}") String chatGptApiKey
  ) {
    EnvironmentVariables.jwtSecretKey = jwtSecretKey;
    EnvironmentVariables.chatGptApiKey = chatGptApiKey;
  }

  public static String getJwtSecretKey() {
    return jwtSecretKey;
  }
  public static String getChatGptApiKey() {
    return chatGptApiKey;
  }
}
