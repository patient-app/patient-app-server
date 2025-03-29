package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
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


    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public Message generateAnswer(Patient patient, String externalConversationId, String message) {

        Optional<Conversation> OptionalConversation = conversationRepository.getConversationByExternalId(externalConversationId);
        Conversation conversation = OptionalConversation.orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation with ID " + externalConversationId + " not found");
        }

        String key = CryptographyUtil.decrypt(patient.getPrivateKey());

        //Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));

        String answer = "generated answer";

        newMessage.setResponse(CryptographyUtil.encrypt(answer, key));
        newMessage.setConversation(conversation);

        Message savedMessage = messageRepository.save(newMessage);
        messageRepository.flush();
        Message extractedMessage = messageRepository.findById(savedMessage.getId()).orElseThrow(() -> new IllegalArgumentException("Message not found"));

        Conversation refreshedConversation = conversationRepository.getConversationByExternalId(externalConversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        //Make a frontend version
        newMessage.setResponse(answer);
        newMessage.setRequest(message);
        newMessage.setExternalConversationId(refreshedConversation.getExternalId());
        System.out.println("Timestamp1: "+ newMessage.getCreatedAt());
        if( newMessage.getCreatedAt() == null){
            LocalDateTime now = LocalDateTime.now();
            newMessage.setCreatedAt(now);
        }
        System.out.println("Timestamp2: "+ newMessage.getCreatedAt());

        return newMessage;
    }
}
