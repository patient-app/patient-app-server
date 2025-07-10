package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateChatbotDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.ChatbotConfigurationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseChatbotOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ChatbotMapper {
    ChatbotMapper INSTANCE = Mappers.getMapper(ChatbotMapper.class);

    ChatbotTemplate convertCreateChatbotDTOToChatbotTemplate(CreateChatbotDTO createChatbotDTO);
    ChatbotConfigurationOutputDTO convertChatbotTemplateToChatbotConfigurationOutputDTO(ChatbotTemplate chatbotTemplate);
    List<ChatbotConfigurationOutputDTO> chatbotTemplatesToChatbotConfigurationOutputDTOs(List<ChatbotTemplate> chatbotTemplates);
    void updateChatbotTemplateFromUpdateChatbotDTO(UpdateChatbotDTO updateChatbotDTO, @MappingTarget ChatbotTemplate chatbotTemplate);


}
