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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class CoachMeetingController {

    private final MeetingService meetingService;

    public CoachMeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/coach/patients/{patientId}/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public MeetingOutputDTO createMeeting(@PathVariable String patientId,
            @RequestBody CreateMeetingDTO createMeeting) {
        System.out.println("someone wants to create a meeting for patient: " + patientId);
        Meeting newMeeting = meetingService.createMeeting(createMeeting, patientId);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(newMeeting);
    }

    @GetMapping("/coach/patients/{patientId}/meetings")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<MeetingOutputDTO> listMeetings(@PathVariable String patientId) {
        List<Meeting> meetings = meetingService.getAllMeetings(patientId);
        return meetings.stream().map(MeetingMapper.INSTANCE::convertEntityToMeetingOutputDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/coach/patients/{patientId}/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public MeetingOutputDTO getMeeting(@PathVariable String patientId, @PathVariable String meetingId) {
        Meeting meeting = meetingService.getMeeting(meetingId, patientId);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(meeting);
    }

    @PutMapping("/coach/patients/{patientId}/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public MeetingOutputDTO updateMeeting(@PathVariable String patientId, @PathVariable String meetingId,
            @RequestBody UpdateMeetingDTO updateMeeting) {
        Meeting updated = meetingService.updateMeeting(patientId, meetingId, updateMeeting);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(updated);
    }

    @DeleteMapping("/coach/patients/{patientId}/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deleteMeeting(@PathVariable String patientId, @PathVariable String meetingId) {
        meetingService.deleteMeeting(patientId, meetingId);
    }
}
