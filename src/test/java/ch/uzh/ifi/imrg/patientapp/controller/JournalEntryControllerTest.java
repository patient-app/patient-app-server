package ch.uzh.ifi.imrg.patientapp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalChatbotOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.containsInAnyOrder;

@ExtendWith(MockitoExtension.class)
public class JournalEntryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private JournalEntryService journalEntryService;

        @Mock
        private PatientService patientService;

        @Mock
        private HttpServletRequest request;

        @Mock
        private LogService logService;

        @InjectMocks
        private JournalEntryController controller;

        @BeforeEach
        void setup() {
                var validator = new LocalValidatorFactoryBean();
                validator.afterPropertiesSet();

                mockMvc = MockMvcBuilders
                                .standaloneSetup(controller)
                                .setValidator(validator)
                                .build();
        }

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
                outDto.setCreatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));
                outDto.setUpdatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));

                when(journalEntryService.createEntry(any(JournalEntryRequestDTO.class), any(Patient.class)))
                                .thenReturn(outDto);
                doNothing().when(logService).createLog(nullable(String.class), any(LogTypes.class), anyString(), "");
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
                m1.setCreatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));
                m1.setUpdatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));

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
                outDto.setCreatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));
                outDto.setUpdatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));

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
                outDto.setCreatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));
                outDto.setUpdatedAt(Instant.parse("2025-06-26T10:15:30+02:00"));

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

        @Test
        void getAllMessages_returnsChatbotOutput() throws Exception {
                // Arrange
                Patient mockPatient = new Patient();
                when(patientService.getCurrentlyLoggedInPatient(any()))
                                .thenReturn(mockPatient);

                // Build a fake JournalChatbotOutputDTO
                JournalChatbotOutputDTO chatDto = new JournalChatbotOutputDTO();
                chatDto.setId("e5");
                chatDto.setName("ChatbotSessionName");

                // Mock the service call
                when(journalEntryService.getJournalChatbot(mockPatient, "e5"))
                                .thenReturn(chatDto);

                // Act & Assert
                mockMvc.perform(get("/patients/journal-entries/e5/chatbot"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("e5"))
                                .andExpect(jsonPath("$.name").value("ChatbotSessionName"));
        }
}
