package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.*;
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

    private final PromptBuilderService promptBuilderService;
    private final AuthorizationService authorizationService;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository,
            PromptBuilderService promptBuilderService, AuthorizationService authorizationService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.promptBuilderService = promptBuilderService;
        this.authorizationService = authorizationService;
    }

    private static List<Map<String, String>> parseMessagesFromConversation(Conversation conversation,
            String key) {
        List<Map<String, String>> priorMessages = new ArrayList<>();

        for (Message msg : conversation.getMessages()) {
            if (msg.getRequest() != null && !msg.getRequest().trim().isEmpty()&& !msg.isInSystemPromptSummary()) {
                String decryptedRequest = CryptographyUtil.decrypt(msg.getRequest(), key);
                priorMessages.add(Map.of(
                        "role", "user",
                        "content", decryptedRequest.trim())) ;
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
        int summaryThreshold = 150;
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);

        Conversation conversation = optionalConversation
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        authorizationService.checkConversationAccess(conversation, patient,
                "You are trying to send a message to another persons chat.");
        String key = CryptographyUtil.decrypt(patient.getPrivateKey());
        // Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        List<Map<String, String>> priorMessages = parseMessagesFromConversation(conversation, key);
        if(priorMessages.size()> summaryThreshold) {
            List<Map<String, String>> oldMessages = priorMessages.subList( 0, priorMessages.size() - 10);
            conversation.setSystemPrompt(promptBuilderService.getSummary(oldMessages, conversation.getSystemPrompt()));
            priorMessages = priorMessages.subList(priorMessages.size() - 10, priorMessages.size());

            //get all messages that are not in the system prompt summary
            // set all except the newest 10 messages to inSystemPromptSummary = true
        }

        String rawAnswer = promptBuilderService.getResponse(priorMessages, message, conversation.getSystemPrompt());

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

        Conversation refreshedConversation = conversationRepository.findById(conversationId)
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
