package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestQuestionInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestQuestionOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PsychologicalTestMapper {
    PsychologicalTestMapper INSTANCE = Mappers.getMapper(PsychologicalTestMapper.class);

    PsychologicalTest convertPsychologicalTestInputDTOToPsychologicalTest(PsychologicalTestInputDTO dto);
    PsychologicalTestQuestions convertPsychologicalTestQuestionInputDTOToPsychologicalTestQuestions(PsychologicalTestQuestionInputDTO dto);
    List<PsychologicalTestQuestions> convertPsychologicalTestQuestionInputDTOsToPsychologicalTestQuestions(List<PsychologicalTestQuestionInputDTO> dtos);

    PsychologicalTestOutputDTO convertPsychologicalTestToPsychologicalTestOutputDTO(PsychologicalTest entity);
    PsychologicalTestQuestionOutputDTO convertPsychologicalTestQuestionToPsychologicalTestQuestionOutputDTO(PsychologicalTestQuestions entity);
    List<PsychologicalTestQuestionOutputDTO> convertPsychologicalTestQuestionsToPsychologicalTestQuestionOutputDTOs(List<PsychologicalTestQuestions> entities);

}
