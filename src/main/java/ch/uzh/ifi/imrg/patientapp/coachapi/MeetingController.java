package ch.uzh.ifi.imrg.patientapp.coachapi;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MeetingOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MeetingMapper;
import ch.uzh.ifi.imrg.patientapp.service.MeetingService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MeetingController {

    private final MeetingService meetingService;
    private final PatientService patientService;

    public MeetingController(MeetingService meetingService, PatientService patientService) {
        this.meetingService = meetingService;
        this.patientService = patientService;
    }

    @PostMapping("/patients/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingOutputDTO createMeeting(HttpServletRequest httpServletRequest,
            @RequestBody CreateMeetingDTO createMeeting) {
        Patient patient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Meeting newMeeting = meetingService.createMeeting(createMeeting, patient.getId());
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(newMeeting);
    }

    @GetMapping("/patients/meetings")
    @ResponseStatus(HttpStatus.OK)
    public List<MeetingOutputDTO> listMeetings(HttpServletRequest httpServletRequest) {
        Patient patient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        List<Meeting> meetings = meetingService.getAllMeetings(patient.getId());
        return meetings.stream().map(MeetingMapper.INSTANCE::convertEntityToMeetingOutputDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/patients/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.OK)
    public MeetingOutputDTO getMeeting(@PathVariable String meetingId, HttpServletRequest httpServletRequest) {
        Patient patient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Meeting meeting = meetingService.getMeeting(meetingId, patient.getId());
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(meeting);
    }

    @PutMapping("/patients/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.OK)
    public MeetingOutputDTO updateMeeting(@PathVariable String meetingId,
            @RequestBody UpdateMeetingDTO updateMeeting, HttpServletRequest httpServletRequest) {
        Patient patient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        Meeting updated = meetingService.updateMeeting(patient.getId(), meetingId, updateMeeting);
        return MeetingMapper.INSTANCE.convertEntityToMeetingOutputDTO(updated);
    }

    @DeleteMapping("/patients/meetings/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeeting(@PathVariable String meetingId, HttpServletRequest httpServletRequest) {
        Patient patient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        meetingService.deleteMeeting(patient.getId(), meetingId);
    }
}
