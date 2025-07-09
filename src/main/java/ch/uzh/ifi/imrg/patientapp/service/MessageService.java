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
import java.util.*;
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

    static List<Map<String, String>> parseMessagesFromConversation(Conversation conversation,
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
        int summaryThreshold = 100; // Must be double of number of messages to summarize
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);

        Conversation conversation = optionalConversation
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        authorizationService.checkConversationAccess(conversation, patient,
                "You are trying to send a message to another persons chat.");
        String key = CryptographyUtil.decrypt(patient.getPrivateKey());
        // Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        String rawHarm = promptBuilderService.getHarmRating(message);
        String harm = promptBuilderService.extractContentFromResponse(rawHarm);
        if(harm.equals("true")){
            System.out.println("Message contains harmful content.");
        }

        List<Map<String, String>> priorMessages = parseMessagesFromConversation(conversation, key);
        if(priorMessages.size() > summaryThreshold) {
            List<Map<String, String>> oldMessages = priorMessages.subList( 0, priorMessages.size() - 20);
            System.out.println("oldMessages:"+ oldMessages);
            String rawSummary = promptBuilderService.getSummary(oldMessages, conversation.getChatSummary());
            conversation.setChatSummary(promptBuilderService.extractContentFromResponse(rawSummary));
            priorMessages = priorMessages.subList(priorMessages.size() - 20, priorMessages.size());

            List<Message> conversationMessages = messageRepository.findByConversationIdAndInSystemPromptSummaryFalseOrderByCreatedAt(conversationId);
            // Mark all except the last 10 as summarized
            int messagesToSummarize = conversationMessages.size() - 10;
            if (messagesToSummarize > 0) {
                for (Message m : conversationMessages.subList(0, messagesToSummarize)) {
                    m.setInSystemPromptSummary(true);
                }
                messageRepository.flush();

            }

        }

        String rawAnswer = promptBuilderService.getResponse(priorMessages, message, conversation.getSystemPrompt());

        String answer = promptBuilderService.extractContentFromResponse(rawAnswer);

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
