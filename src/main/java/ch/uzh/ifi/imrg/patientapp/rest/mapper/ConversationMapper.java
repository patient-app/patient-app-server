package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateConversationDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.CompleteExerciseConversationOutputDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConversationMapper {
        ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

        GeneralConversation createConversationDTOToConversation(CreateConversationDTO createConversationDTO);
        @Mapping(source = "shareWithAi", target = "shareWithAi")
        @Mapping(source = "shareWithCoach", target = "shareWithCoach")
        @Mapping(source= "conversationName", target = "name")
        CompleteConversationOutputDTO convertEntityToCompleteConversationOutputDTO(GeneralConversation conversation);
        CompleteExerciseConversationOutputDTO convertEntityToCompleteExerciseConversationOutputDTO(ExerciseConversation conversation);

        @Mapping(source= "conversationName", target = "name")
        NameConversationOutputDTO convertEntityToNameConversationOutputDTO(GeneralConversation conversation);

        List<NameConversationOutputDTO> convertEntityListToNameConversationOutputDTOList(
                        List<GeneralConversation> conversations);

        @Mapping(source = "shareWithAi", target = "shareWithAi")
        @Mapping(source = "shareWithCoach", target = "shareWithCoach")
        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        void updateConversationFromPutSharingDTO(PutSharingDTO putSharingDTO,
                        @MappingTarget GeneralConversation conversation);
}
