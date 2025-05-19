package ch.uzh.ifi.imrg.patientapp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentVariablesTest {

    @Test
    void constructor_shouldSetStaticFieldsCorrectly() {
        String jwtKey = "test-jwt-key";
        String gptKey = "test-chatgpt-key";
        String localAIKey = "test-local-ai-key";

        // Simulate Spring injecting the values
        new EnvironmentVariables(jwtKey, gptKey, localAIKey);

        assertEquals(jwtKey, EnvironmentVariables.getJwtSecretKey());
        assertEquals(gptKey, EnvironmentVariables.getChatGptApiKey());
        assertEquals(localAIKey, EnvironmentVariables.getLocalLlmApiKey());
    }

    @Test
    void getChatGptApiKey_shouldReturnNullBeforeInitialization() {
        assertNull(EnvironmentVariables.getChatGptApiKey());
    }

}
