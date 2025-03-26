package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Message;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MessageOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageMapper {
    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);


    @Mapping(source ="externalId", target = "id")
    @Mapping(source = "externalConversationId", target = "conversationId")
    @Mapping(source = "response", target = "message")
    @Mapping(source = "createdAt", target = "timestamp")
    MessageOutputDTO convertEntityToMessageOutputDTO(Message message);

}
