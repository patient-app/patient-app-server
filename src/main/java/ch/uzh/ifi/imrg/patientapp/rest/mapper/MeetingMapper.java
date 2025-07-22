package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MeetingOutputDTO;

@Mapper
public interface MeetingMapper {

    MeetingMapper INSTANCE = Mappers.getMapper(MeetingMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "startAt", target = "startAt")
    @Mapping(source = "endAt", target = "endAt")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "meetingStatus", target = "meetingStatus")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    MeetingOutputDTO convertEntityToMeetingOutputDTO(Meeting meeting);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "startAt", target = "startAt")
    @Mapping(source = "endAt", target = "endAt")
    @Mapping(source = "meetingStatus", target = "meetingStatus")
    @Mapping(source = "location", target = "location")
    Meeting convertCreateMeetingDTOToEntity(CreateMeetingDTO createMeetingDTO);

}
