package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EnvironmentVariables {

  private static String jwtSecretKey;
  private static String chatGptApiKey;
  private static String localLlmApiKey;
  private static String appCookieDomain;


  @Autowired
  public EnvironmentVariables(@Value("${JWT_SECRET_KEY}") String jwtSecretKey,
                              @Value("${CHATGPT_API_KEY}") String chatGptApiKey,
                              @Value("${LOCAL_AI_API_KEY}") String localLlmApiKey,
                              @Value("${APP_COOKIE_DOMAIN:}") String appCookieDomain
  ) {
    EnvironmentVariables.jwtSecretKey = jwtSecretKey;
    EnvironmentVariables.chatGptApiKey = chatGptApiKey;
    EnvironmentVariables.localLlmApiKey = localLlmApiKey;
    EnvironmentVariables.appCookieDomain = appCookieDomain;
  }

  public static String getJwtSecretKey() {
    return jwtSecretKey;
  }
  public static String getLocalLlmApiKey(){return localLlmApiKey;}
  public static String getChatGptApiKey() {
    return chatGptApiKey;
  }
  public static String getAppCookieDomain(){
    return appCookieDomain;
  }
}
