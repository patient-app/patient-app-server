package ch.uzh.ifi.imrg.patientapp.utils;

public final class WelcomeMessageUtil {

    private WelcomeMessageUtil() {
    }

    public static String getDocumentWelcomeMessage(String language) {
        String lang = (language == null) ? "" : language.toLowerCase();
        switch (lang) {
            case "uk":
                return "Welcome to the document chatbot, what could be a good welcome message for this?";
            default:
                return "Welcome to the document chatbot, what could be a good welcome message for this?";
        }
    }

    public static String getExerciseWelcomeMessage(String language) {
        String lang = (language == null) ? "" : language.toLowerCase();
        switch (lang) {
            case "uk":
                return "Welcome to the excercise chatbot, what could be a good welcome message for this?";
            default:
                return "Welcome to the excercise chatbot, what could be a good welcome message for this?";
        }
    }

    public static String getJournalEntryWelcomeMessage(String language) {
        String lang = (language == null) ? "" : language.toLowerCase();
        switch (lang) {
            case "uk":
                return "Welcome to the journal chatbot, what could be a good welcome message for this?";
            default:
                return "Welcome to the journal chatbot, what could be a good welcome message for this?";
        }
    }
}
