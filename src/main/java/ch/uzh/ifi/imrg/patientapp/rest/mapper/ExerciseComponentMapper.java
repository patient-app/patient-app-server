package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseComponentMapper {
    ExerciseComponentMapper INSTANCE = Mappers.getMapper(ExerciseComponentMapper.class);

    ExerciseComponent exerciseComponentInputDTOToExerciseComponent(
            ExerciseComponentInputDTO exerciseComponentInputDTO);

    List<ExerciseComponentOverviewOutputDTO> exerciseComponentsToExerciseComponentsOverviewOutputDTOs(
            List<ExerciseComponent> exerciseComponents);

    @Mapping(target = "id", ignore = true) // Never overwrite IDs
    @Mapping(target = "createdAt", ignore = true) // Never overwrite creation timestamp
    @Mapping(target = "updatedAt", ignore = true) // Let @UpdateTimestamp handle this
    @Mapping(target = "exercise", ignore = true) // Usually set relationships explicitly
    void updateExerciseComponentFromExerciseComponentInputDTO(ExerciseComponentInputDTO dto, @MappingTarget ExerciseComponent entity);

    void updateExerciseComponentFromExerciseComponentResultInputDTO(ExerciseComponentResultInputDTO dto, @MappingTarget ExerciseComponent entity);
}
