package ch.uzh.ifi.imrg.patientapp.utils;

import java.util.Base64;

import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;

public class DocumentChatbotUtil {

    // TODO: Fix this
    public static String extractContext(PatientDocument pd) {
        byte[] data = pd.getDocument().getData();
        // Fallback: Base64-encode first N bytes for a human-readable snippet
        int N = Math.min(data.length, 256);
        String snippet = Base64.getEncoder().encodeToString(java.util.Arrays.copyOf(data, N));
        return snippet + (data.length > N ? "..." : "");
    }

}
