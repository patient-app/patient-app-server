package ch.uzh.ifi.imrg.patientapp.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.MeetingRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.MeetingMapper;

@Service
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final PatientRepository patientRepository;

    public MeetingService(MeetingRepository meetingRepository, PatientRepository patientRepository) {
        this.meetingRepository = meetingRepository;
        this.patientRepository = patientRepository;
    }

    public Meeting createMeeting(CreateMeetingDTO createMeeting, String PatientId) {
        Patient patient = patientRepository.getPatientById(PatientId);
        if (patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id " + PatientId);
        }
        Meeting newMeeting = MeetingMapper.INSTANCE.convertCreateMeetingDTOToEntity(createMeeting);
        newMeeting.setPatient(patient);
        Meeting savedMeeting = meetingRepository.save(newMeeting);
        return savedMeeting;
    }

    public List<Meeting> getAllMeetings(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Patient not found with id " + patientId);
        }
        return meetingRepository.findByPatientId(patientId);
    }

    public Meeting getMeeting(String meetingId, String patientId) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found with id " + meetingId));

        if (!meeting.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Meeting %s not found for patient %s", meetingId, patientId));
        }
        return meeting;
    }

    public Meeting updateMeeting(String patientId, String meetingId, UpdateMeetingDTO updateMeeting) {

        Meeting meeting = getMeeting(meetingId, patientId);

        if (updateMeeting.getStartAt() != null) {
            meeting.setStartAt(updateMeeting.getStartAt());
        }
        if (updateMeeting.getEndAt() != null) {
            meeting.setEndAt(updateMeeting.getEndAt());
        }
        if (updateMeeting.getLocation() != null) {
            meeting.setLocation(updateMeeting.getLocation());
        }
        if (updateMeeting.getMeetingStatus() != null) {
            meeting.setMeetingStatus(updateMeeting.getMeetingStatus());
        }

        return meetingRepository.save(meeting);
    }

    public void deleteMeeting(String patientId, String meetingId) {
        Meeting meeting = getMeeting(meetingId, patientId);
        meetingRepository.delete(meeting);
    }

}
