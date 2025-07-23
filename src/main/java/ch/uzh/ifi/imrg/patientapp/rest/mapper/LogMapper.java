package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import ch.uzh.ifi.imrg.patientapp.entity.Log;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.LogOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LogMapper {
    LogMapper INSTANCE = Mappers.getMapper(LogMapper.class);
    LogOutputDTO convertEntityToLogOutputDTO(Log log);
    List<LogOutputDTO> convertEntitiesToLogOutputDTOs(List<Log> logs);
}
