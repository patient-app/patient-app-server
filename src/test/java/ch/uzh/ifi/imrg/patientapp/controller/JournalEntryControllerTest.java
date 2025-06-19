package ch.uzh.ifi.imrg.patientapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.containsInAnyOrder;

@WebMvcTest(JournalEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class JournalEntryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private JournalEntryService journalEntryService;

        @MockitoBean
        private PatientService patientService;

        @Mock
        private HttpServletRequest request;

        @Test
        void createEntry_returnsCreatedEntry() throws Exception {
                // Arrange
                JournalEntryRequestDTO reqDto = new JournalEntryRequestDTO();
                reqDto.setTitle("My Title");
                reqDto.setContent("Some content");
                reqDto.setTags(Set.of("tag1", "tag2"));
                reqDto.setSharedWithTherapist(true);
                reqDto.setAiAccessAllowed(false);

                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);

                JournalEntryOutputDTO outDto = new JournalEntryOutputDTO();
                outDto.setId("e1");
                outDto.setTitle("My Title");
                outDto.setContent("Some content");
                outDto.setTags(Set.of("tag1", "tag2"));
                outDto.setSharedWithTherapist(true);
                outDto.setAiAccessAllowed(false);
                outDto.setCreatedAt(LocalDateTime.of(2025, 6, 20, 12, 0));
                outDto.setUpdatedAt(LocalDateTime.of(2025, 6, 20, 12, 0));

                when(journalEntryService.createEntry(any(JournalEntryRequestDTO.class), any(Patient.class)))
                                .thenReturn(outDto);

                // Act & Assert
                mockMvc.perform(post("/patients/journal-entries")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("e1"))
                                .andExpect(jsonPath("$.title").value("My Title"))
                                .andExpect(jsonPath("$.content").value("Some content"))
                                .andExpect(jsonPath("$.tags").isArray())
                                .andExpect(jsonPath("$.sharedWithTherapist").value(true))
                                .andExpect(jsonPath("$.aiAccessAllowed").value(false));
        }

        @Test
        void getAllTags_returnsTagSet() throws Exception {
                // Arrange
                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);
                when(journalEntryService.getAllTags(mockPatient)).thenReturn(Set.of("tagA", "tagB"));

                // Act & Assert
                mockMvc.perform(get("/patients/journal-entries/tags"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$", containsInAnyOrder("tagA", "tagB")));
        }

        @Test
        void listAll_returnsMetaList() throws Exception {
                // Arrange
                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);

                GetAllJournalEntriesDTO m1 = new GetAllJournalEntriesDTO();
                m1.setId("e1");
                m1.setTitle("T1");
                m1.setTags(Set.of("t1"));
                m1.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));
                m1.setUpdatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));

                when(journalEntryService.listEntries(mockPatient)).thenReturn(List.of(m1));

                // Act & Assert
                mockMvc.perform(get("/patients/journal-entries"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].id").value("e1"));
        }

        @Test
        void getOne_returnsFullEntry() throws Exception {
                // Arrange
                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);

                JournalEntryOutputDTO outDto = new JournalEntryOutputDTO();
                outDto.setId("e2");
                outDto.setTitle("Title2");
                outDto.setContent("Content2");
                outDto.setTags(Set.of("x", "y"));
                outDto.setSharedWithTherapist(false);
                outDto.setAiAccessAllowed(true);
                outDto.setCreatedAt(LocalDateTime.of(2025, 6, 2, 11, 0));
                outDto.setUpdatedAt(LocalDateTime.of(2025, 6, 2, 11, 0));

                when(journalEntryService.getEntry(mockPatient, "e2")).thenReturn(outDto);

                // Act & Assert
                mockMvc.perform(get("/patients/journal-entries/e2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("e2"))
                                .andExpect(jsonPath("$.content").value("Content2"));
        }

        @Test
        void updateEntry_returnsUpdatedEntry() throws Exception {
                // Arrange
                JournalEntryRequestDTO reqDto = new JournalEntryRequestDTO();
                reqDto.setTitle("NewTitle");
                reqDto.setContent("NewContent");
                reqDto.setTags(Set.of("a", "b"));
                reqDto.setSharedWithTherapist(false);
                reqDto.setAiAccessAllowed(true);

                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);

                JournalEntryOutputDTO outDto = new JournalEntryOutputDTO();
                outDto.setId("e3");
                outDto.setTitle("NewTitle");
                outDto.setContent("NewContent");
                outDto.setTags(Set.of("a", "b"));
                outDto.setSharedWithTherapist(false);
                outDto.setAiAccessAllowed(true);
                outDto.setCreatedAt(LocalDateTime.of(2025, 6, 3, 12, 0));
                outDto.setUpdatedAt(LocalDateTime.of(2025, 6, 3, 12, 0));

                when(journalEntryService.updateJournalEntry(any(Patient.class), eq("e3"),
                                any(JournalEntryRequestDTO.class)))
                                .thenReturn(outDto);

                // Act & Assert
                mockMvc.perform(put("/patients/journal-entries/e3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("e3"))
                                .andExpect(jsonPath("$.title").value("NewTitle"));
        }

        @Test
        void deleteEntry_returnsNoContent() throws Exception {
                // Arrange
                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any())).thenReturn(mockPatient);
                doNothing().when(journalEntryService).deleteEntry(mockPatient, "e4");

                // Act & Assert
                mockMvc.perform(delete("/patients/journal-entries/e4"))
                                .andExpect(status().isNoContent());
        }
}
