package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


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

        Conversation conversation = conversationRepository.getConversationByExternalId(externalConversationId);

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

        messageRepository.save(newMessage);
        messageRepository.flush();

        // Add to conversation
        conversation.getMessages().add(newMessage);
        Conversation savedConversation = conversationRepository.save(conversation);
        conversationRepository.flush();


        //Make a frontend version
        newMessage.setResponse(answer);
        newMessage.setRequest(message);
        newMessage.setExternalConversationId(savedConversation.getExternalId());
        System.out.println("Timestamp: "+ newMessage.getCreatedAt());

        return newMessage;
    }
}
