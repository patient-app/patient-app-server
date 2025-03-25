package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConversationMapper {
    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

}
