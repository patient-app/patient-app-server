package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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

    private static List<Map<String, String>> parseMessagesFromConversation(Conversation conversation, String key) {
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


    public Message generateAnswer(Patient patient, String externalConversationId, String message) {

        Optional<Conversation> optionalConversation = conversationRepository.getConversationByExternalId(externalConversationId);
        Conversation conversation = optionalConversation.orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        authorizationService.checkConversationAccess(conversation,patient, "You are trying to send a message to another persons chat.");
        String key = CryptographyUtil.decrypt(patient.getPrivateKey());
        // Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        List<Map<String, String>> priorMessages = parseMessagesFromConversation(conversation,key);
        String answer = promptBuilderService.getResponse(patient.isAdmin(),priorMessages, message);

        newMessage.setResponse(CryptographyUtil.encrypt(answer, key));
        newMessage.setConversation(conversation);
        if (newMessage.getCreatedAt() == null) {
            Instant now = Instant.now();
            newMessage.setCreatedAt(now);
        }
        messageRepository.save(newMessage);
        messageRepository.flush();

        Conversation refreshedConversation = conversationRepository.getConversationByExternalId(externalConversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Make a frontend version
        Message frontendMessage = new Message();

        frontendMessage.setConversation(null);
        frontendMessage.setResponse(answer);
        frontendMessage.setRequest(message);
        frontendMessage.setExternalConversationId(refreshedConversation.getExternalId());
        frontendMessage.setExternalId(newMessage.getExternalId());
        frontendMessage.setCreatedAt(newMessage.getCreatedAt());
        return frontendMessage;
    }
}
