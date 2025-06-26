package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseInformation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.StoredExerciseFile;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
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
    ExerciseInformation exerciseInformationInputDTOToExerciseInformation(ExerciseInformationInputDTO exerciseInformationInputDTO);

    void updateExerciseFromInputDTO(ExerciseInputDTO exerciseInputDTO, @MappingTarget Exercise target);
}


