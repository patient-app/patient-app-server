package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.*;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatbotTemplateRepository chatbotTemplateRepository;

    private final PromptBuilderService promptBuilderService;
    private final AuthorizationService authorizationService;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository,
            ChatbotTemplateRepository chatbotTemplateRepository,
            PromptBuilderService promptBuilderService, AuthorizationService authorizationService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
        this.promptBuilderService = promptBuilderService;
        this.authorizationService = authorizationService;
    }

    private static List<Map<String, String>> parseMessagesFromConversation(GeneralConversation conversation,
            String key) {
        List<Map<String, String>> priorMessages = new ArrayList<>();

        for (Message msg : conversation.getMessages()) {
            if (msg.getRequest() != null && !msg.getRequest().trim().isEmpty()) {
                String decryptedRequest = CryptographyUtil.decrypt(msg.getRequest(), key);
                priorMessages.add(Map.of(
                        "role", "user",
                        "content", decryptedRequest.trim()));
            }
            if (msg.getResponse() != null && !msg.getResponse().trim().isEmpty()) {
                String decryptedResponse = CryptographyUtil.decrypt(msg.getResponse(), key);
                priorMessages.add(Map.of(
                        "role", "assistant",
                        "content", decryptedResponse.trim()));
            }
        }

        return priorMessages;
    }

    public Message generateAnswer(Patient patient, String conversationId, String message) {

        Optional<GeneralConversation> optionalConversation = conversationRepository.findById(conversationId);
        GeneralConversation conversation = optionalConversation
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        authorizationService.checkConversationAccess(conversation, patient,
                "You are trying to send a message to another persons chat.");
        String key = CryptographyUtil.decrypt(patient.getPrivateKey());
        // Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        List<Map<String, String>> priorMessages = parseMessagesFromConversation(conversation, key);
        List<ChatbotTemplate> chatbotTemplates = chatbotTemplateRepository.findByPatientId(patient.getId());
        String rawAnswer = promptBuilderService.getResponse(patient.isAdmin(), priorMessages, message,
                chatbotTemplates.get(0));

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

        newMessage.setResponse(CryptographyUtil.encrypt(answer, key));
        newMessage.setConversation(conversation);
        if (newMessage.getCreatedAt() == null) {
            Instant now = Instant.now();
            newMessage.setCreatedAt(now);
        }
        messageRepository.save(newMessage);
        messageRepository.flush();

        GeneralConversation refreshedConversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Make a frontend version
        Message frontendMessage = new Message();

        frontendMessage.setConversation(null);
        frontendMessage.setResponse(answer);
        frontendMessage.setRequest(message);
        frontendMessage.setExternalConversationId(refreshedConversation.getId());
        frontendMessage.setExternalId(newMessage.getExternalId());
        frontendMessage.setCreatedAt(newMessage.getCreatedAt());
        return frontendMessage;
    }
}
