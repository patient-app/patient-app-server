package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseMediaOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = ExerciseElementMapper.class)
public interface ExerciseMapper {

    List<ExercisesOverviewOutputDTO> exercisesToExerciseOverviewOutputDTOs(List<Exercise> exercises);
    Exercise exerciseInputDTOToExercise(ExerciseInputDTO exerciseInputDTO);
    ExerciseOutputDTO exerciseToExerciseOutputDTO(Exercise exercise);
    ExerciseMediaOutputDTO storedExerciseFileToExerciseMediaOutputDTO(StoredExerciseFile storedExerciseFile);
    List <ExerciseInformationOutputDTO> exerciseInformationsToExerciseInformationOutputDTOs(List<ExerciseInformation> exerciseInformations);

    void updateExerciseFromInputDTO(ExerciseInputDTO exerciseInputDTO, @MappingTarget Exercise target);

    // MapStruct will map the simple fields
    ExerciseInformation mapBaseFields(ExerciseInformationInputDTO dto);

    // Add this as the main mapping entry point:
    default ExerciseInformation exerciseInformationInputDTOToExerciseInformation(ExerciseInformationInputDTO dto) {
        // Map the simple fields
        ExerciseInformation info = mapBaseFields(dto);

        // Build the before container
        if (dto.getMoodsBefore() != null && !dto.getMoodsBefore().isEmpty()) {
            ExerciseMoodContainer beforeContainer = new ExerciseMoodContainer();
            beforeContainer.setExerciseMoods(
                    dto.getMoodsBefore().stream().map(m -> {
                        ExerciseMood mood = new ExerciseMood();
                        mood.setMoodName(m.getMoodName());
                        mood.setMoodScore(m.getMoodScore());
                        mood.setExerciseMoodContainer(beforeContainer);
                        return mood;
                    }).toList()
            );
            info.setExerciseMoodBefore(beforeContainer);
        }

        // Build the after container
        if (dto.getMoodsAfter() != null && !dto.getMoodsAfter().isEmpty()) {
            ExerciseMoodContainer afterContainer = new ExerciseMoodContainer();
            afterContainer.setExerciseMoods(
                    dto.getMoodsAfter().stream().map(m -> {
                        ExerciseMood mood = new ExerciseMood();
                        mood.setMoodName(m.getMoodName());
                        mood.setMoodScore(m.getMoodScore());
                        mood.setExerciseMoodContainer(afterContainer);
                        return mood;
                    }).toList()
            );
            info.setExerciseMoodAfter(afterContainer);
        }

        return info;
    }
}


