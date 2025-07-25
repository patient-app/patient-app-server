package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = ExerciseComponentMapper.class)
public interface ExerciseMapper {

    ExerciseChatbotOutputDTO exerciseConversationToExerciseChatbotOutputDTO(ExerciseConversation exerciseConversation);
    List<ExercisesOverviewOutputDTO> exercisesToExerciseOverviewOutputDTOs(List<Exercise> exercises);
    Exercise exerciseInputDTOToExercise(ExerciseInputDTO exerciseInputDTO);

    @Mapping(target = "exerciseExecutionId", source = "id")
    ExerciseOutputDTO exerciseToExerciseOutputDTO(Exercise exercise);
    List <ExerciseInformationOutputDTO> exerciseInformationsToExerciseInformationOutputDTOs(List<ExerciseCompletionInformation> exerciseInformations);

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

    default ExerciseInformationOutputDTO exerciseInformationToExerciseInformationOutputDTO(ExerciseCompletionInformation entity) {
        ExerciseInformationOutputDTO dto = new ExerciseInformationOutputDTO();

        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setFeedback(entity.getFeedback());

        // Map moods before
        if (entity.getExerciseMoodBefore() != null && entity.getExerciseMoodBefore().getExerciseMoods() != null) {
            dto.setMoodsBefore(
                    entity.getExerciseMoodBefore().getExerciseMoods().stream()
                            .map(m -> {
                                ExerciseMoodOutputDTO moodDto = new ExerciseMoodOutputDTO();
                                moodDto.setMoodName(m.getMoodName());
                                moodDto.setMoodScore(m.getMoodScore());
                                return moodDto;
                            })
                            .toList()
            );
        }

        // Map moods after
        if (entity.getExerciseMoodAfter() != null && entity.getExerciseMoodAfter().getExerciseMoods() != null) {
            dto.setMoodsAfter(
                    entity.getExerciseMoodAfter().getExerciseMoods().stream()
                            .map(m -> {
                                ExerciseMoodOutputDTO moodDto = new ExerciseMoodOutputDTO();
                                moodDto.setMoodName(m.getMoodName());
                                moodDto.setMoodScore(m.getMoodScore());
                                return moodDto;
                            })
                            .toList()
            );
        }

        return dto;
    }

}


