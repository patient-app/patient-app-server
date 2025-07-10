package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Document.DocumentConversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PutSharingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CompleteConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.NameConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentConversationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.CompleteExerciseConversationOutputDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConversationMapper {
        ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

        @Mapping(source = "shareWithAi", target = "shareWithAi")
        @Mapping(source = "shareWithCoach", target = "shareWithCoach")
        CompleteConversationOutputDTO convertEntityToCompleteConversationOutputDTO(GeneralConversation conversation);

        CompleteExerciseConversationOutputDTO convertEntityToCompleteExerciseConversationOutputDTO(
                        ExerciseConversation conversation);

        NameConversationOutputDTO convertEntityToNameConversationOutputDTO(GeneralConversation conversation);

        List<NameConversationOutputDTO> convertEntityListToNameConversationOutputDTOList(
                        List<GeneralConversation> conversations);

        @Mapping(source = "shareWithAi", target = "shareWithAi")
        @Mapping(source = "shareWithCoach", target = "shareWithCoach")
        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        void updateConversationFromPutSharingDTO(PutSharingDTO putSharingDTO,
                        @MappingTarget GeneralConversation conversation);

        DocumentConversationOutputDTO convertEntityToDocumentConversationOutputDTO(DocumentConversation conversation);
}
