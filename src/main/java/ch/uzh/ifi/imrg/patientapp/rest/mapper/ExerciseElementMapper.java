package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseFileElementOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseImageElementOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExerciseElementMapper {
    ExerciseElementMapper INSTANCE = Mappers.getMapper(ExerciseElementMapper.class);

    ExerciseComponent exerciseComponentInputDTOToExerciseComponent(
            ExerciseComponentInputDTO exerciseComponentInputDTO);

    ExerciseComponentsOverviewOutputDTO exerciseComponentsToExerciseComponentsOverviewOutputDTOs(
            ExerciseComponent exerciseComponent);

}
