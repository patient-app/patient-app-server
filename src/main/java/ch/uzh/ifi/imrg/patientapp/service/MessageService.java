package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.MessageRepository;

public class MessageService {

    private final MessageRepository messageRepository;


    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message generateAnswer(Patient patient, String message, String conversationId) {
        Message newMessage = new Message();
        newMessage.setRequest(message);
        String answer = "generated answer";
        newMessage.setResponse(answer);
        messageRepository.save(newMessage);
        // Add to conversation
        return newMessage;
    }
}
