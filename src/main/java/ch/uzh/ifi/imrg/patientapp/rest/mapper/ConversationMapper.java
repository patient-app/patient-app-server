package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConversationMapper {
    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);
    Conversation convertCreateConversationDTOToEntity(CreateConversationDTO createConversationDTO);

    ChatbotOutputDTO convertEntityToCreateChatbotOutputDTO(Conversation conversation);

}
