package ch.uzh.ifi.imrg.patientapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.MeetingOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.MeetingService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MeetingControllerTest {

    @Mock
    private MeetingService meetingService;

    @Mock
    private PatientService patientService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MeetingController meetingController;

    @Test
    void createMeeting_shouldReturnOutputDTO() {
        CreateMeetingDTO input = new CreateMeetingDTO(); // populate if needed
        Patient patient = new Patient();
        patient.setId("patient1");

        Meeting meeting = new Meeting();
        meeting.setId("m1");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(meetingService.createMeeting(input, patient.getId())).thenReturn(meeting);

        MeetingOutputDTO result = meetingController.createMeeting(request, input);

        assertNotNull(result);
        assertEquals("m1", result.getId());
    }

    @Test
    void listMeetings_shouldReturnListOfOutputDTOs() {
        Patient patient = new Patient();
        patient.setId("patient1");

        Meeting m1 = new Meeting();
        m1.setId("m1");
        Meeting m2 = new Meeting();
        m2.setId("m2");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(meetingService.getAllMeetings(patient.getId())).thenReturn(Arrays.asList(m1, m2));

        List<MeetingOutputDTO> results = meetingController.listMeetings(request);

        assertEquals(2, results.size());
        assertEquals("m1", results.get(0).getId());
        assertEquals("m2", results.get(1).getId());
    }

    @Test
    void getMeeting_shouldReturnMappedOutputDTO() {
        Patient patient = new Patient();
        patient.setId("patient1");

        Meeting meeting = new Meeting();
        meeting.setId("m1");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(meetingService.getMeeting("m1", "patient1")).thenReturn(meeting);

        MeetingOutputDTO result = meetingController.getMeeting("m1", request);

        assertNotNull(result);
        assertEquals("m1", result.getId());
    }

    @Test
    void updateMeeting_shouldReturnUpdatedOutputDTO() {
        Patient patient = new Patient();
        patient.setId("patient1");

        UpdateMeetingDTO input = new UpdateMeetingDTO(); // populate if needed
        Meeting updatedMeeting = new Meeting();
        updatedMeeting.setId("m1");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);
        when(meetingService.updateMeeting("patient1", "m1", input)).thenReturn(updatedMeeting);

        MeetingOutputDTO result = meetingController.updateMeeting("m1", input, request);

        assertNotNull(result);
        assertEquals("m1", result.getId());
    }

    @Test
    void deleteMeeting_shouldCallService() {
        Patient patient = new Patient();
        patient.setId("patient1");

        when(patientService.getCurrentlyLoggedInPatient(request)).thenReturn(patient);

        meetingController.deleteMeeting("m1", request);

        verify(meetingService).deleteMeeting("patient1", "m1");
    }
}

