package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PatientOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientMapper {

    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "onboarded", target = "onboarded")
    @Mapping(source = "chatBotAvatar", target = "chatBotAvatar")
    PatientOutputDTO convertEntityToPatientOutputDTO(Patient patient);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "coachAccessKey", target = "coachAccessKey")
    @Mapping(source = "coachEmail", target = "coachEmail")
    Patient convertCreatePatientDTOToEntity(CreatePatientDTO createPatientDTO);
}
