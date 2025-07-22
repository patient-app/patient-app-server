package ch.uzh.ifi.imrg.patientapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.MeetingRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private MeetingService meetingService;

    private final String patientId = "patient-1";
    private final String meetingId = "meeting-1";

    @Test
    void createMeeting_success() {
        CreateMeetingDTO dto = new CreateMeetingDTO();
        dto.setId("id1");
        dto.setStartAt(Instant.parse("2025-06-15T10:00:00Z"));
        dto.setEndAt(Instant.parse("2025-06-15T11:00:00Z"));
        dto.setLocation("Room A");

        Patient patient = new Patient();
        patient.setId(patientId);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        Meeting savedMeeting = new Meeting();
        savedMeeting.setId(meetingId);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(savedMeeting);

        Meeting result = meetingService.createMeeting(dto, patientId);
        assertNotNull(result);
        assertEquals(meetingId, result.getId());

        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void createMeeting_patientNotFound_throws() {
        CreateMeetingDTO dto = new CreateMeetingDTO();
        when(patientRepository.getPatientById(patientId)).thenReturn(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingService.createMeeting(dto, patientId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getAllMeetings_success() {
        when(patientRepository.existsById(patientId)).thenReturn(true);
        List<Meeting> meetings = Arrays.asList(new Meeting(), new Meeting());
        when(meetingRepository.findByPatientId(patientId)).thenReturn(meetings);

        List<Meeting> result = meetingService.getAllMeetings(patientId);
        assertEquals(2, result.size());
    }

    @Test
    void getAllMeetings_patientNotFound_throws() {
        when(patientRepository.existsById(patientId)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingService.getAllMeetings(patientId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getMeeting_success() {
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        Patient patient = new Patient();
        patient.setId(patientId);
        meeting.setPatient(patient);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        Meeting result = meetingService.getMeeting(meetingId, patientId);
        assertEquals(meeting, result);
    }

    @Test
    void getMeeting_notFound_throws() {
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingService.getMeeting(meetingId, patientId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getMeeting_wrongPatient_throws() {
        Meeting meeting = new Meeting();
        meeting.setPatient(new Patient());
        meeting.getPatient().setId("other");
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingService.getMeeting(meetingId, patientId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateMeeting_success() {
        Meeting existing = new Meeting();
        existing.setId(meetingId);
        Patient patient = new Patient();
        patient.setId(patientId);
        existing.setPatient(patient);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));

        UpdateMeetingDTO dto = new UpdateMeetingDTO();
        dto.setLocation("New Room");
        Meeting saved = new Meeting();
        saved.setId(meetingId);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(saved);

        Meeting result = meetingService.updateMeeting(patientId, meetingId, dto);
        assertEquals(meetingId, result.getId());
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void deleteMeeting_success() {
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        Patient patient = new Patient();
        patient.setId(patientId);
        meeting.setPatient(patient);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        meetingService.deleteMeeting(patientId, meetingId);
        verify(meetingRepository).delete(meeting);
    }
}
