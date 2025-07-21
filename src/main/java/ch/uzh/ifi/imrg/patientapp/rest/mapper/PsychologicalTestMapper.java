package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest.PsychologicalTest;
import PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestQuestionInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestQuestionOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PsychologicalTestMapper {
    PsychologicalTestMapper INSTANCE = Mappers.getMapper(PsychologicalTestMapper.class);

    @Mapping(source = "questions", target = "psychologicalTestsQuestions")
    PsychologicalTest convertPsychologicalTestInputDTOToPsychologicalTest(PsychologicalTestInputDTO dto);
    PsychologicalTestQuestions convertPsychologicalTestQuestionInputDTOToPsychologicalTestQuestions(PsychologicalTestQuestionInputDTO dto);
    List<PsychologicalTestQuestions> convertPsychologicalTestQuestionInputDTOsToPsychologicalTestQuestions(List<PsychologicalTestQuestionInputDTO> dtos);


    @Mapping(source = "psychologicalTestsQuestions", target = "questions")
    @Mapping(source = "patient.id", target = "patientId")
    PsychologicalTestOutputDTO convertPsychologicalTestToPsychologicalTestOutputDTO(PsychologicalTest entity);
    PsychologicalTestQuestionOutputDTO convertPsychologicalTestQuestionToPsychologicalTestQuestionOutputDTO(PsychologicalTestQuestions entity);
    List<PsychologicalTestQuestionOutputDTO> convertPsychologicalTestQuestionsToPsychologicalTestQuestionOutputDTOs(List<PsychologicalTestQuestions> entities);

}
