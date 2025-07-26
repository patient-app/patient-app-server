package ch.uzh.ifi.imrg.patientapp.service.aiService;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PromptBuilderService {
    private final ChatGPTService chatGPTService;

    public PromptBuilderService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    public String getSystemPrompt(ChatbotTemplate chatbotTemplate, Patient patient) {
        String language = extractLanguage(patient);
        String context = chatbotTemplate.getChatbotContext();
        if (context == null || context.trim().isEmpty()) {
            context = "No additional context provided.";
        }

        return String.format(
                "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human called %s, that needs someone to talk to. You can be a friend, a family member, a therapist, or anyone else. You can ask questions, give advice, or just listen. Remember, you are not a therapist, but a friend. Please keep your own responses to the person short and in %s. No longer than 200 characters.\nAdditional Context:\n%s",
                chatbotTemplate.getChatbotRole(),
                chatbotTemplate.getChatbotTone(),
                patient.getName(),
                language,
                context);

    }

    public String getSystemPrompt(ChatbotTemplate chatbotTemplate, String context, Patient patient) {
        String language = extractLanguage(patient);
        return String.format(
                "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human called %s, that needs someone to talk to. You should help the person to understand this exercise: %s When answering questions about the exercise only use the description from before. It is really important to stick to this description. Please keep your own responses to the person short and in %s. No longer than 400 characters.",
                chatbotTemplate.getChatbotRole(),
                chatbotTemplate.getChatbotTone(),
                patient.getName(),
                language,
                context);

    }

    public String getJournalSystemPrompt(ChatbotTemplate chatbotTemplate, String journalTitle, String journalContent,
            Patient patient) {
        String language = extractLanguage(patient);

        return String.format(
                "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human called %s, that needs someone to talk to. You should help the person to reflect on her/his journal entry (%s): %s. When answering questions about the journal mostly use the content from the journal. It is really important to stick to this journal. Please keep your own responses to the person short and in %s. No longer than 400 characters.",
                chatbotTemplate.getChatbotRole(),
                chatbotTemplate.getChatbotTone(),
                patient.getName(),
                journalTitle,
                journalContent,
                language);

    }

    public String getDocumentSystemPrompt(ChatbotTemplate chatbotTemplate, String documentContext, Patient patient) {
        String language = extractLanguage(patient);
        return String.format(
                "Act as a %s, who cares about the other person. Your tone should be %s. You will interact with a human called %s, that needs someone to talk to. You should help the person to understand this document: %s When answering questions about the document only use the content from the document. It is really important to stick to this document. Please keep your own responses to the person short and in %s. No longer than 400 characters.",
                chatbotTemplate.getChatbotRole(),
                chatbotTemplate.getChatbotTone(),
                patient.getName(),
                documentContext,
                language);
    }

    public String getResponse(List<Map<String, String>> priorMessages, String message, String systemPrompt) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt));

        // Add prior chat history
        if (priorMessages != null) {
            messages.addAll(priorMessages);
        }

        // Current user message
        messages.add(Map.of(
                "role", "user",
                "content", message));

        return extractContentFromResponse(chatGPTService.getResponse(messages));
    }

    public String getSummary(List<Map<String, String>> allMessages, String oldSummary) {
        List<Map<String, String>> messages = new ArrayList<>();

        String systemPrompt;

        if (oldSummary == null || oldSummary.isBlank()) {
            systemPrompt = "Summarize the following conversation in a few sentences. "
                    + "Do not use bullet points or lists. Just write a short summary of the conversation. "
                    + "The summary should be no longer than 200 characters.\n"
                    + "Messages: ";
        } else {
            systemPrompt = "Update the following existing summary by including the additional messages below. "
                    + "Keep it short (no more than 200 additional characters), do not use bullet points or lists.\n\n"
                    + "Existing summary:\n" + oldSummary + "\n\n"
                    + "Additional messages:\n";
        }

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt));

        // Add prior chat history
        if (allMessages != null) {
            messages.addAll(allMessages);
        }

        return extractContentFromResponse(chatGPTService.getResponse(messages));
    }

    public String getSummaryOfAllConversations(List<String> conversationSummaries) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt: explain what you want *at the meta level*
        messages.add(Map.of(
                "role", "system",
                "content",
                "You are an assistant that summarizes multiple conversation summaries into a single concise summary. " +
                        "Do not use bullet points or lists. Just write a short summary no longer than 100 words."));

        // Combine all summaries into a clear single string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < conversationSummaries.size(); i++) {
            sb.append("Summary ").append(i + 1).append(": ").append(conversationSummaries.get(i)).append("\n");
        }

        messages.add(Map.of(
                "role", "user",
                "content", "Here are the conversation summaries:\n\n" + sb));

        return extractContentFromResponse(chatGPTService.getResponse(messages));
    }

    public String getHarmRating(String message) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of(
                "role", "system",
                "content",
                "You are a classifier that detects suicide risk and self harm in text messages. Respond ONLY with 'true' or 'false'. If either appear Respond with 'true'."));

        // Current user message
        messages.add(Map.of(
                "role", "user",
                "content", message));

        return extractContentFromResponse(chatGPTService.getResponse(messages));
    }

    private String extractContentFromResponse(String rawAnswer) {
        // extract the answer part from the response
        String regex = "</think>\\s*([\\s\\S]*)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(rawAnswer);

        String answer;

        if (matcher.find()) {
            answer = matcher.group(1).trim();
        } else {
            throw new IllegalStateException("No <think> closing tag found in response:\n" + rawAnswer);
        }
        return answer;
    }

    public String extractLanguage(Patient patient) {
        String patientLanguage = patient.getLanguage();
        if (patientLanguage == null) {
            return "english";
        }
        String language;
        if (patientLanguage.equals("uk")) {
            language = "ukrainian";
        } else if (patientLanguage.equals("de")) {
            language = "german";
        } else {
            language = "english";
        }
        return language;
    }

}
