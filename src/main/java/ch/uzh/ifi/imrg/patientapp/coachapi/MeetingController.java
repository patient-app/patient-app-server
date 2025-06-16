package ch.uzh.ifi.imrg.patientapp.coachapi;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MeetingOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MeetingMapper;
import ch.uzh.ifi.imrg.patientapp.service.MeetingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/patients/{patientId}/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingOutputDTO createMeeting(HttpServletRequest httpServletRequest, @PathVariable String patientId,
            @RequestBody CreateMeetingDTO createMeeting) {
        Meeting newMeeting = meetingService.createMeeting(createMeeting, patientId);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(newMeeting);
    }

    @GetMapping("/patients/{patientId}/meetings")
    @ResponseStatus(HttpStatus.OK)
    public List<MeetingOutputDTO> listMeetings(@PathVariable String patientId) {
        List<Meeting> meetings = meetingService.getAllMeetings(patientId);
        return meetings.stream().map(MeetingMapper.INSTANCE::convertEntityToMeetingOutputDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/patients/{patientId}/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.OK)
    public MeetingOutputDTO getMeeting(@PathVariable String patientId, @PathVariable String meetingId) {
        Meeting meeting = meetingService.getMeeting(meetingId, patientId);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(meeting);
    }

    @PutMapping("/patients/{patientId}/meetings/{meetingId}")
    public MeetingOutputDTO updateMeeting(@PathVariable String patientId, @PathVariable String meetingId,
            @RequestBody UpdateMeetingDTO updateMeeting) {
        Meeting updated = meetingService.updateMeeting(patientId, meetingId, updateMeeting);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(updated);
    }

    @DeleteMapping("/patients/{patientId}/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeeting(@PathVariable String patientId, @PathVariable String meetingId) {
        meetingService.deleteMeeting(patientId, meetingId);
    }
}
