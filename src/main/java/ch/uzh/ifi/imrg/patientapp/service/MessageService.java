package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ConversationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;

public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;


    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public Message generateAnswer(Patient patient, String message, String externalConversationId) {

        Conversation conversation = conversationRepository.getConversationByExternalId(externalConversationId);
        String key = CryptographyUtil.decrypt(patient.getPrivateKey());
        //Make message persistent
        Message newMessage = new Message();
        newMessage.setRequest(CryptographyUtil.encrypt(message, key));
        String answer = "generated answer";
        newMessage.setResponse(CryptographyUtil.encrypt(answer, key));
        newMessage.setConversation(conversation);
        Message savedMessage = this.messageRepository.save(newMessage);

        //Make a frontend version
        Message frontendMessage = new Message();
        frontendMessage.setExternalId(savedMessage.getExternalId());
        frontendMessage.setResponse(answer);
        frontendMessage.setExternalConversationId(externalConversationId);
        frontendMessage.setCreatedAt(savedMessage.getCreatedAt());

        // Add to conversation
        conversation.getMessages().add(newMessage);
        conversationRepository.save(conversation);
        return frontendMessage;
    }
}
