package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = ExerciseComponentMapper.class)
public interface ExerciseMapper {

    ExerciseChatbotOutputDTO exerciseConversationToExerciseChatbotOutputDTO(ExerciseConversation exerciseConversation);
    List<ExercisesOverviewOutputDTO> exercisesToExerciseOverviewOutputDTOs(List<Exercise> exercises);
    Exercise exerciseInputDTOToExercise(ExerciseInputDTO exerciseInputDTO);



    SharedInputFieldOutputDTO exerciseComponentAnswerToSharedInputFieldOutputDTO(ExerciseComponentAnswer exerciseComponentAnswer);
    List<SharedInputFieldOutputDTO> exerciseComponentAnswersToSharedInputFieldOutputDTOs(List<ExerciseComponentAnswer> exerciseComponentAnswers);

    @Mapping(target = "exerciseExecutionId", source = "id")
    ExerciseOutputDTO exerciseToExerciseOutputDTO(Exercise exercise);

    @Mapping(target = "sharedInputFields", source = "componentAnswers")
    @Mapping(target = "moodsBefore", expression = "java(moodContainerToDTOs(entity.getExerciseMoodBefore()))")
    @Mapping(target = "moodsAfter", expression = "java(moodContainerToDTOs(entity.getExerciseMoodAfter()))")
    ExerciseInformationOutputDTO exerciseInformationToExerciseInformationOutputDTO(ExerciseCompletionInformation entity);

    List<ExerciseInformationOutputDTO> exerciseInformationsToExerciseInformationOutputDTOs(List<ExerciseCompletionInformation> exerciseInformations);

    ExerciseMoodOutputDTO exerciseMoodToExerciseMoodOutputDTO(ExerciseMood mood);
    List<ExerciseMoodOutputDTO> exerciseMoodsToExerciseMoodOutputDTOs(List<ExerciseMood> moods);

    default List<ExerciseMoodOutputDTO> moodContainerToDTOs(ExerciseMoodContainer container) {
        if (container == null || container.getExerciseMoods() == null) {
            return java.util.Collections.emptyList();
        }
        return exerciseMoodsToExerciseMoodOutputDTOs(container.getExerciseMoods());
    }


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExerciseFromInputDTO(ExerciseUpdateInputDTO exerciseUpdateInputDTO, @MappingTarget Exercise target);

    @Mapping(target = "exerciseMoodBefore", ignore = true)
    @Mapping(target = "exerciseMoodAfter", ignore = true)
    void updateExerciseCompletionInformationFromExerciseCompletionInformation(ExerciseInformationInputDTO dto, @MappingTarget ExerciseCompletionInformation entity);

    ExerciseMood exerciseMoodInputDTOToExerciseMood(ExerciseMoodInputDTO dto);

    List<ExerciseMood> mapMoodInputDTOsToExerciseMoods(List<ExerciseMoodInputDTO> dtos);

    ExerciseCompletionInformation exerciseComponentResultInputDTOToExerciseCompletionInformation(ExerciseComponentResultInputDTO exerciseComponentResultInputDTO);

    @Mapping(source="id", target="exerciseExecutionId")
    ExecutionOverviewOutputDTO exerciseCompletionInformationToExecutionOverviewOutputDTO(ExerciseCompletionInformation info);

    List<ExecutionOverviewOutputDTO> exerciseCompletionInformationsToExecutionOverviewOutputDTOs(List<ExerciseCompletionInformation> exerciseInformations);

}


