package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConversationMapper {
    ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

    @Mapping(source = "externalId", target = "id")
    @Mapping(source = "messages", target = "messages")
    CompleteConversationOutputDTO convertEntityToCompleteConversationOutputDTO(Conversation conversation);
}
