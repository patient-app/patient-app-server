package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConversationMapper {
    ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

    @Mapping(source = "externalId", target = "id")
    CompleteConversationOutputDTO convertEntityToCompleteConversationOutputDTO(Conversation conversation);


    @Mapping(source = "externalId", target = "id")
    @Mapping(source = "name", target = "name")
    NameConversationOutputDTO convertEntityToNameConversationOutputDTO(Conversation conversation);

    List<NameConversationOutputDTO> convertEntityListToNameConversationOutputDTOList(List<Conversation> conversations);
}
