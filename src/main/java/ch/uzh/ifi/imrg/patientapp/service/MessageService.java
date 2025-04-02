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

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    private final PromptBuilderService promptBuilderService;


    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository, PromptBuilderService promptBuilderService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.promptBuilderService = promptBuilderService;
    }

    public Message generateAnswer(Patient patient, String externalConversationId, String message) {

        Optional<Conversation> optionalConversation = conversationRepository.getConversationByExternalId(externalConversationId);
        Conversation conversation = optionalConversation.orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        //Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        String answer = promptBuilderService.getResponse();

        newMessage.setResponse(CryptographyUtil.encrypt(answer, key));
        newMessage.setConversation(conversation);

        Message savedMessage = messageRepository.save(newMessage);
        messageRepository.flush();

        Conversation refreshedConversation = conversationRepository.getConversationByExternalId(externalConversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        //Make a frontend version
        newMessage.setResponse(answer);
        newMessage.setRequest(message);
        newMessage.setExternalConversationId(refreshedConversation.getExternalId());

        if( newMessage.getCreatedAt() == null){
            LocalDateTime now = LocalDateTime.now();
            newMessage.setCreatedAt(now);
        }
        return newMessage;
    }
}
