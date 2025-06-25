package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseFileElementOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseImageElementOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExerciseElementMapper {

    @Mapping(source = "name", target = "data.name")
    @Mapping(source = "fileId", target = "data.id")
    ExerciseFileElementOutputDTO toDto(ExerciseFileElement entity);

    @Mapping(source = "fileId", target = "data.id")
    @Mapping(source = "alt", target = "data.alt")
    ExerciseImageElementOutputDTO toDto(ExerciseImageElement entity);

    @Mapping(source = "label", target = "data.label")
    @Mapping(source = "placeholder", target = "data.placeholder")
    @Mapping(source = "required", target = "data.required")
    ExerciseTextInputElementOutputDTO toDto(ExerciseTextElement entity);

    @Mapping(source = "data.label", target = "label")
    @Mapping(source = "data.placeholder", target = "placeholder")
    @Mapping(source = "data.required", target = "required")
    ExerciseTextElement toEntity(ExerciseTextInputElementInputDTO dto);

    // Map stored file ID to String
    default String map(StoredExerciseFile storedFile) {
        return storedFile != null ? storedFile.getId() : null;
    }

    default ExerciseFileElement toEntity(ExerciseFileElementInputDTO dto) {
        ExerciseFileElement entity = new ExerciseFileElement();
        entity.setId(dto.getId());
        entity.setName(dto.getData().getName());

        String base64 = dto.getData().toString();

        StoredExerciseFile file = new StoredExerciseFile();
        file.setId(UUID.randomUUID().toString());
        file.setData(base64.getBytes(StandardCharsets.UTF_8)); // or decode if needed

        entity.setFileId(file);

        return entity;
    }

    default ExerciseImageElement toEntity(ExerciseImageElementInputDTO dto) {
        ExerciseImageElement entity = new ExerciseImageElement();
        entity.setId(dto.getId());
        entity.setAlt(dto.getData().getAlt());

        String base64 = dto.getData().toString();

        StoredExerciseFile file = new StoredExerciseFile();
        file.setId(UUID.randomUUID().toString());
        file.setData(base64.getBytes(StandardCharsets.UTF_8)); // or decode if needed

        entity.setFileId(file);

        return entity;
    }


    // Dispatcher entity
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

    // Dispatcher DTO
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
