package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EnvironmentVariables {

  private static String jwtSecretKey;
  private static String chatGptApiKey;
  private static String localLlmApiKey;


  @Autowired
  public EnvironmentVariables(@Value("${JWT_SECRET_KEY}") String jwtSecretKey,
                              @Value("${CHATGPT_API_KEY}") String chatGptApiKey,
                              @Value("${LOCAL_AI_API_KEY}") String localLlmApiKey
  ) {
    EnvironmentVariables.jwtSecretKey = jwtSecretKey;
    EnvironmentVariables.chatGptApiKey = chatGptApiKey;
    EnvironmentVariables.localLlmApiKey = localLlmApiKey;
  }

  public static String getJwtSecretKey() {
    return jwtSecretKey;
  }
  public static String getLocalLlmApiKey(){return localLlmApiKey;}
  public static String getChatGptApiKey() {
    return chatGptApiKey;
  }
}
