package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import org.hibernate.annotations.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


@Mapper
public interface MessageMapper {
    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);


    @Mapping(source ="externalId", target = "id")
    @Mapping(source = "externalConversationId", target = "conversationId")
    @Mapping(source = "response", target = "responseMessage")
    @Mapping(source = "request", target = "requestMessage")
    @Mapping(source = "createdAt", target = "timestamp")
    MessageOutputDTO convertEntityToMessageOutputDTO(Message message);

}
