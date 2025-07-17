package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseComponentMapper {
    ExerciseComponentMapper INSTANCE = Mappers.getMapper(ExerciseComponentMapper.class);

    ExerciseComponent exerciseComponentInputDTOToExerciseComponent(
            ExerciseComponentInputDTO exerciseComponentInputDTO);

    ExerciseComponentOverviewOutputDTO exerciseComponentToExerciseComponentOverviewOutputDTO(ExerciseComponent entity);

    List<ExerciseComponentOverviewOutputDTO> exerciseComponentsToExerciseComponentsOverviewOutputDTOs(
            List<ExerciseComponent> exerciseComponents);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExerciseComponentFromExerciseComponentInputDTO(ExerciseComponentInputDTO dto, @MappingTarget ExerciseComponent entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExerciseComponentFromExerciseComponentUpdateInputDTO(ExerciseComponentUpdateInputDTO dto, @MappingTarget ExerciseComponent entity);
}
