package ch.uzh.ifi.imrg.patientapp.coachapi;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Set;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachGetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.CoachJournalEntryOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.JournalEntryService;

@ExtendWith(MockitoExtension.class)
class CoachJournalEntryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private JournalEntryService journalEntryService;

    @InjectMocks
    private CoachJournalEntryController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void listAll_shouldReturnAllJournalEntries() throws Exception {
        String patientId = "patient123";

        CoachGetAllJournalEntriesDTO dto1 = new CoachGetAllJournalEntriesDTO();
        dto1.setId("e1");
        dto1.setCreatedAt(Instant.parse("2025-06-25T10:00:00Z"));
        dto1.setUpdatedAt(Instant.parse("2025-06-25T12:00:00Z"));
        dto1.setTitle("First entry");
        dto1.setTags(Set.of("a", "b"));

        CoachGetAllJournalEntriesDTO dto2 = new CoachGetAllJournalEntriesDTO();
        dto2.setId("e2");
        dto2.setCreatedAt(Instant.parse("2025-06-26T09:30:00Z"));
        dto2.setUpdatedAt(Instant.parse("2025-06-26T10:00:00Z"));
        dto2.setTitle("Second entry");
        dto2.setTags(Set.of("x"));

        List<CoachGetAllJournalEntriesDTO> list = List.of(dto1, dto2);

        when(journalEntryService.getEntriesForCoach(eq(patientId)))
                .thenReturn(list);

        mockMvc.perform(get("/coach/patients/{patientId}/journal-entries", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value("e1"))
                .andExpect(jsonPath("$[0].createdAt").value("2025-06-25T10:00:00Z"))
                .andExpect(jsonPath("$[0].updatedAt").value("2025-06-25T12:00:00Z"))
                .andExpect(jsonPath("$[0].title").value("First entry"))
                .andExpect(jsonPath("$[0].tags.length()").value(2))

                .andExpect(jsonPath("$[1].id").value("e2"))
                .andExpect(jsonPath("$[1].createdAt").value("2025-06-26T09:30:00Z"))
                .andExpect(jsonPath("$[1].updatedAt").value("2025-06-26T10:00:00Z"))
                .andExpect(jsonPath("$[1].title").value("Second entry"))
                .andExpect(jsonPath("$[1].tags.length()").value(1));
    }

    @Test
    void getOne_shouldReturnSingleJournalEntry() throws Exception {
        String patientId = "patient123";
        String entryId = "e1";

        CoachJournalEntryOutputDTO dto = new CoachJournalEntryOutputDTO();
        dto.setId(entryId);
        dto.setCreatedAt(Instant.parse("2025-06-25T10:00:00Z"));
        dto.setUpdatedAt(Instant.parse("2025-06-25T11:00:00Z"));
        dto.setTitle("Detailed entry");
        dto.setContent("Here is the full content of the entry.");
        dto.setTags(Set.of("tag1", "tag2"));

        when(journalEntryService.getOneEntryForCoach(eq(patientId), eq(entryId)))
                .thenReturn(dto);

        mockMvc.perform(get("/coach/patients/{patientId}/journal-entries/{entryId}", patientId, entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("e1"))
                .andExpect(jsonPath("$.createdAt").value("2025-06-25T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2025-06-25T11:00:00Z"))
                .andExpect(jsonPath("$.title").value("Detailed entry"))
                .andExpect(jsonPath("$.content").value("Here is the full content of the entry."))
                .andExpect(jsonPath("$.tags.length()").value(2));
    }
}
