package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseFileElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseImageElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseTextInputElement;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseElementDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseFileElementDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseImageElementDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseTextInputElementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExerciseElementMapper {

    @Mapping(source = "fileName", target = "data.name")
    @Mapping(source = "fileUrl", target = "data.url")
    ExerciseFileElementDTO toDto(ExerciseFileElement entity);

    @Mapping(source = "imageUrl", target = "data.url")
    @Mapping(source = "altText", target = "data.alt")
    ExerciseImageElementDTO toDto(ExerciseImageElement entity);

    @Mapping(source = "label", target = "data.label")
    @Mapping(source = "placeholder", target = "data.placeholder")
    @Mapping(source = "required", target = "data.required")
    ExerciseTextInputElementDTO toDto(ExerciseTextInputElementDTO entity);

    // Main dispatcher
    default ExerciseElementDTO toDto(ExerciseElement element) {
        if (element instanceof ExerciseFileElement) {
            return toDto((ExerciseFileElement) element);
        } else if (element instanceof ExerciseImageElement) {
            return toDto((ExerciseImageElement) element);
        } else if (element instanceof ExerciseTextInputElementDTO) {
        return toDto((ExerciseTextInputElementDTO) element); // ✅ ENTITY subclass
    }

        throw new IllegalArgumentException("Unsupported ExerciseElement subtype: " + element.getClass());
    }
}
