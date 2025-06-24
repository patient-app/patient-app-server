package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseFileElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseImageElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseTextElement;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseFileElementOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseImageElementOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExerciseElementMapper {

    @Mapping(source = "name", target = "data.name")
    @Mapping(source = "url", target = "data.url")
    ExerciseFileElementOutputDTO toDto(ExerciseFileElement entity);

    @Mapping(source = "url", target = "data.url")
    @Mapping(source = "alt", target = "data.alt")
    ExerciseImageElementOutputDTO toDto(ExerciseImageElement entity);

    @Mapping(source = "label", target = "data.label")
    @Mapping(source = "placeholder", target = "data.placeholder")
    @Mapping(source = "required", target = "data.required")
    ExerciseTextInputElementOutputDTO toDto(ExerciseTextElement entity);

    @Mapping(source = "data.name", target = "name")
    @Mapping(source = "data.url", target = "url")
    ExerciseFileElement toEntity(ExerciseFileElementInputDTO dto);

    @Mapping(source = "data.url", target = "url")
    @Mapping(source = "data.alt", target = "alt")
    ExerciseImageElement toEntity(ExerciseImageElementInputDTO dto);

    @Mapping(source = "data.label", target = "label")
    @Mapping(source = "data.placeholder", target = "placeholder")
    @Mapping(source = "data.required", target = "required")
    ExerciseTextElement toEntity(ExerciseTextInputElementInputDTO dto);

    // Dispatcher
    default ExerciseElement toEntity(ExerciseElementInputDTO dto) {
        if (dto instanceof ExerciseFileElementInputDTO fileDto) {
            return toEntity(fileDto);
        } else if (dto instanceof ExerciseImageElementInputDTO imageDto) {
            return toEntity(imageDto);
        } else if (dto instanceof ExerciseTextInputElementInputDTO textDto) {
            return toEntity(textDto);
        }
        throw new IllegalArgumentException("Unsupported type: " + dto.getClass());
    }

    // Main dispatcher
    default ExerciseElementOutputDTO toDto(ExerciseElement element) {
        if (element instanceof ExerciseFileElement) {
            return toDto((ExerciseFileElement) element);
        } else if (element instanceof ExerciseImageElement) {
            return toDto((ExerciseImageElement) element);
        } else if (element instanceof ExerciseTextElement) {
        return toDto((ExerciseTextElement) element);
    }

        throw new IllegalArgumentException("Unsupported ExerciseElement subtype: " + element.getClass());
    }
}
