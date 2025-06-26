package ch.uzh.ifi.imrg.patientapp.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ch.uzh.ifi.imrg.patientapp.coachapi.CoachMeetingController;
import ch.uzh.ifi.imrg.patientapp.constant.MeetingStatus;
import ch.uzh.ifi.imrg.patientapp.entity.Meeting;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateMeetingDTO;
import ch.uzh.ifi.imrg.patientapp.service.MeetingService;

@ExtendWith(MockitoExtension.class)
public class CoachMeetingControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private MeetingService meetingService;

        @InjectMocks
        private CoachMeetingController meetingController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(meetingController)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build();
        }

        @Test
        void createMeeting_shouldReturnCreatedMeeting() throws Exception {
                String patientId = "p1";
                CreateMeetingDTO dto = new CreateMeetingDTO();
                dto.setExternalMeetingId("ext1");
                dto.setStartAt(Instant.parse("2025-06-15T10:00:00Z"));
                dto.setEndAt(Instant.parse("2025-06-15T11:00:00Z"));
                dto.setLocation("Room 101");

                Meeting saved = new Meeting();
                saved.setId("m1");
                saved.setExternalMeetingId("ext1");
                saved.setStartAt(dto.getStartAt());
                saved.setEndAt(dto.getEndAt());
                saved.setLocation("Room 101");
                saved.setMeetingStatus(MeetingStatus.PENDING);
                saved.setCreatedAt(Instant.parse("2025-06-15T09:00:00"));
                Patient patient = new Patient();
                patient.setId(patientId);
                saved.setPatient(patient);

                when(meetingService.createMeeting(any(CreateMeetingDTO.class), eq(patientId)))
                                .thenReturn(saved);

                mockMvc.perform(post("/coach/patients/{patientId}/meetings", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("m1"))
                                .andExpect(jsonPath("$.externalMeetingId").value("ext1"))
                                .andExpect(jsonPath("$.patientId").value(patientId))
                                .andExpect(jsonPath("$.startAt").value("2025-06-15T10:00:00Z"))
                                .andExpect(jsonPath("$.endAt").value("2025-06-15T11:00:00Z"))
                                .andExpect(jsonPath("$.location").value("Room 101"))
                                .andExpect(jsonPath("$.meetingStatus").value("PENDING"))
                                .andExpect(jsonPath("$.createdAt").value("2025-06-15T09:00:00"));
        }

        @Test
        void listMeetings_shouldReturnAllMeetings() throws Exception {
                String patientId = "p1";
                Meeting m1 = new Meeting();
                m1.setId("m1");
                Meeting m2 = new Meeting();
                m2.setId("m2");
                List<Meeting> meetings = Arrays.asList(m1, m2);

                when(meetingService.getAllMeetings(patientId)).thenReturn(meetings);

                mockMvc.perform(get("/coach/patients/{patientId}/meetings", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value("m1"))
                                .andExpect(jsonPath("$[1].id").value("m2"));
        }

        @Test
        void getMeeting_shouldReturnMeeting() throws Exception {
                String patientId = "p1";
                String meetingId = "m1";
                Meeting m = new Meeting();
                m.setId(meetingId);
                m.setExternalMeetingId("ext1");

                when(meetingService.getMeeting(meetingId, patientId)).thenReturn(m);

                mockMvc.perform(get("/coach/patients/{patientId}/meetings/{meetingId}", patientId, meetingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(meetingId))
                                .andExpect(jsonPath("$.externalMeetingId").value("ext1"));
        }

        @Test
        void updateMeeting_shouldReturnUpdatedMeeting() throws Exception {
                String patientId = "p1";
                String meetingId = "m1";
                UpdateMeetingDTO dto = new UpdateMeetingDTO();
                dto.setStartAt(Instant.parse("2025-06-15T12:00:00Z"));
                dto.setEndAt(Instant.parse("2025-06-15T13:00:00Z"));
                dto.setLocation("Room 103");
                dto.setMeetingStatus(MeetingStatus.CONFIRMED);

                Meeting updated = new Meeting();
                updated.setId(meetingId);
                updated.setExternalMeetingId("ext1");
                updated.setStartAt(dto.getStartAt());
                updated.setEndAt(dto.getEndAt());
                updated.setLocation("Room 103");
                updated.setMeetingStatus(MeetingStatus.CONFIRMED);
                updated.setCreatedAt(Instant.parse("2025-06-15T09:00:00"));

                when(meetingService.updateMeeting(eq(patientId), eq(meetingId), any(UpdateMeetingDTO.class)))
                                .thenReturn(updated);

                mockMvc.perform(put("/coach/patients/{patientId}/meetings/{meetingId}", patientId, meetingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(meetingId))
                                .andExpect(jsonPath("$.location").value("Room 103"))
                                .andExpect(jsonPath("$.meetingStatus").value("CONFIRMED"));
        }

        @Test
        void deleteMeeting_shouldReturnNoContent() throws Exception {
                String patientId = "p1";
                String meetingId = "m1";

                doNothing().when(meetingService).deleteMeeting(patientId, meetingId);

                mockMvc.perform(delete("/coach/patients/{patientId}/meetings/{meetingId}", patientId, meetingId))
                                .andExpect(status().isNoContent());
        }
}