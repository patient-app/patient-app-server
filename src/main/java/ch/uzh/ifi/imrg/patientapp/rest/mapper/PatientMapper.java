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
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "age", target = "age")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "description", target = "description")
    PatientOutputDTO convertEntityToPatientOutputDTO(Patient patient);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    Patient convertCreatePatientDtoToEntity(CreatePatientDTO createPatientDTO);
}
