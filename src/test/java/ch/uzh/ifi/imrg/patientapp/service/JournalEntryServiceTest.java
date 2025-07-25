package ch.uzh.ifi.imrg.patientapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.JournalEntryConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ChatbotTemplateRepository;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.*;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.CryptographyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository repo;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private LogService logService;

    @InjectMocks
    private JournalEntryService service;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("p1");
        patient.setPrivateKey("encKey");
    }

    @Test
    void createEntry_encryptsAndDecryptsCorrectly() {
        // given
        JournalEntryRequestDTO dto = new JournalEntryRequestDTO();
        dto.setTitle("Title");
        dto.setContent("Content");
        dto.setTags(Set.of("t1", "t2"));
        dto.setSharedWithTherapist(true);
        dto.setAiAccessAllowed(false);

        JournalEntry savedEntity = new JournalEntry();
        savedEntity.setId("e1");
        savedEntity.setPatient(patient);
        savedEntity.setTitle(dto.getTitle());
        savedEntity.setContent(dto.getContent());
        savedEntity.setTags(dto.getTags());
        savedEntity.setSharedWithTherapist(true);
        savedEntity.setAiAccessAllowed(false);

        when(repo.saveAndFlush(any(JournalEntry.class))).thenReturn(savedEntity);

        ChatbotTemplate template = new ChatbotTemplate();
        template.setId("tmpl1");
        when(chatbotTemplateRepository.findByPatientId("p1"))
                .thenReturn(List.of(template));

        when(promptBuilderService.getJournalSystemPrompt(
                eq(template),
                eq("Title"),
                eq("Content"),eq(patient)))
                .thenReturn("<<SYSTEM PROMPT>>");

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            // decrypt privateKey
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            // encrypt returns input unmodified for test simplicity
            crypto.when(() -> CryptographyUtil.encrypt(anyString(), eq("rawKey")))
                    .thenAnswer(inv -> inv.getArgument(0));
            // decrypt returns input unmodified
            crypto.when(() -> CryptographyUtil.decrypt(anyString(), eq("rawKey")))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            JournalEntryOutputDTO out = service.createEntry(dto, patient);

            // then: fields match savedEntity
            assertEquals("e1", out.getId());
            assertEquals("Title", out.getTitle());
            assertEquals("Content", out.getContent());
            assertEquals(dto.getTags(), out.getTags());
            assertTrue(out.isSharedWithTherapist());
            assertFalse(out.isAiAccessAllowed());
        }
    }


    @Test
    void getAllTags_decryptsTags() {
        // given encrypted tags
        Set<String> encrypted = Set.of("encA", "encB");
        when(repo.findDistinctTagsByPatientId("p1")).thenReturn(encrypted);

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.decrypt("encA", "rawKey")).thenReturn("A");
            crypto.when(() -> CryptographyUtil.decrypt("encB", "rawKey")).thenReturn("B");

            // when
            Set<String> tags = service.getAllTags(patient);

            // then
            assertEquals(Set.of("A", "B"), tags);
        }
    }

    @Test
    void listEntries_mapsAndDecryptsMeta() {
        // given one entry
        JournalEntry entry = new JournalEntry();
        entry.setId("e2");
        entry.setPatient(patient);
        entry.setTitle("encTitle");
        entry.setTags(Set.of("encTag"));
        when(repo.findByPatientId("p1")).thenReturn(List.of(entry));

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.decrypt("encTitle", "rawKey")).thenReturn("Title");
            crypto.when(() -> CryptographyUtil.decrypt("encTag", "rawKey")).thenReturn("Tag");

            // when
            List<GetAllJournalEntriesDTO> list = service.listEntries(patient);

            // then
            assertEquals(1, list.size());
            GetAllJournalEntriesDTO dto = list.get(0);
            assertEquals("e2", dto.getId());
            assertEquals("Title", dto.getTitle());
            assertEquals(Set.of("Tag"), dto.getTags());
        }
    }

    @Test
    void getEntry_existing_decryptsAndReturns() {
        // given
        JournalEntry entry = new JournalEntry();
        entry.setId("e3");
        entry.setPatient(patient);
        entry.setTitle("encT");
        entry.setContent("encC");
        entry.setTags(Set.of("encX"));
        when(repo.findById("e3")).thenReturn(Optional.of(entry));

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.decrypt("encT", "rawKey")).thenReturn("T");
            crypto.when(() -> CryptographyUtil.decrypt("encC", "rawKey")).thenReturn("C");
            crypto.when(() -> CryptographyUtil.decrypt("encX", "rawKey")).thenReturn("X");

            // when
            JournalEntryOutputDTO dto = service.getEntry(patient, "e3");

            // then
            assertEquals("e3", dto.getId());
            assertEquals("T", dto.getTitle());
            assertEquals("C", dto.getContent());
            assertEquals(Set.of("X"), dto.getTags());
        }
    }

    @Test
    void getEntry_notFound_throws() {
        when(repo.findById("nope")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getEntry(patient, "nope"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateJournalEntry_overwritesAndDecrypts() {
        // given existing
        JournalEntry entry = new JournalEntry();
        entry.setId("e4");
        entry.setPatient(patient);
        when(repo.findById("e4")).thenReturn(Optional.of(entry));

        JournalEntryRequestDTO dto = new JournalEntryRequestDTO();
        dto.setTitle("newT");
        dto.setContent("newC");
        dto.setTags(Set.of("tag1"));
        dto.setSharedWithTherapist(false);
        dto.setAiAccessAllowed(true);

        // simulate save returns updated entry with encrypted fields
        JournalEntry updated = new JournalEntry();
        updated.setId("e4");
        updated.setPatient(patient);
        updated.setTitle("encNewT");
        updated.setContent("encNewC");
        updated.setTags(Set.of("encTag1"));
        when(repo.saveAndFlush(entry)).thenReturn(updated);

        doNothing().when(logService).createLog(anyString(), any(LogTypes.class), anyString(), eq(""));

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.encrypt("newT", "rawKey")).thenReturn("encNewT");
            crypto.when(() -> CryptographyUtil.encrypt("newC", "rawKey")).thenReturn("encNewC");
            crypto.when(() -> CryptographyUtil.encrypt("tag1", "rawKey")).thenReturn("encTag1");
            crypto.when(() -> CryptographyUtil.decrypt("encNewT", "rawKey")).thenReturn("newT");
            crypto.when(() -> CryptographyUtil.decrypt("encNewC", "rawKey")).thenReturn("newC");
            crypto.when(() -> CryptographyUtil.decrypt("encTag1", "rawKey")).thenReturn("tag1");

            // when
            JournalEntryOutputDTO out = service.updateJournalEntry(patient, "e4", dto);

            // then
            assertEquals("e4", out.getId());
            assertEquals("newT", out.getTitle());
            assertEquals("newC", out.getContent());
            assertEquals(Set.of("tag1"), out.getTags());
        }
    }

    @Test
    void getEntry_shouldThrowNotFound_whenPatientMismatch() {
        // Arrange
        String entryId = "entry-456";

        Patient callingPatient = new Patient();
        callingPatient.setId("caller-id");

        Patient actualOwner = new Patient();
        actualOwner.setId("owner-id");

        JournalEntry entry = new JournalEntry();
        entry.setId(entryId);
        entry.setPatient(actualOwner);

        when(repo.findById(entryId)).thenReturn(Optional.of(entry));

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getEntry(callingPatient, entryId));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Entry not found for that patient", ex.getReason());
    }


    @Test
    void updateJournalEntry_shouldThrowNotFound_whenPatientMismatch() {
        // Arrange
        String entryId = "entry-123";
        JournalEntryRequestDTO dto = new JournalEntryRequestDTO();

        Patient caller = new Patient();
        caller.setId("caller-id");

        Patient entryOwner = new Patient();
        entryOwner.setId("owner-id");

        JournalEntry entry = new JournalEntry();
        entry.setId(entryId);
        entry.setPatient(entryOwner);

        when(repo.findById(entryId)).thenReturn(Optional.of(entry));

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateJournalEntry(caller, entryId, dto));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Entry not found for this patient", ex.getReason());

        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void deleteEntry_shouldThrowNotFound_whenPatientMismatch() {
        // Given
        Patient callingPatient = new Patient();
        callingPatient.setId("caller-id");

        Patient actualOwner = new Patient();
        actualOwner.setId("owner-id");

        JournalEntry entry = new JournalEntry();
        entry.setId("entry-1");
        entry.setPatient(actualOwner);

        when(repo.findById("entry-1"))
                .thenReturn(Optional.of(entry));

        // When + Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.deleteEntry(callingPatient, "entry-1"));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Entry not found for this patient", exception.getReason());

        verify(repo, never()).delete(any());
    }

    @Test
    void deleteEntry_existing_deletes() {
        JournalEntry entry = new JournalEntry();
        entry.setId("e5");
        entry.setPatient(patient);
        when(repo.findById("e5")).thenReturn(Optional.of(entry));
        // when
        assertDoesNotThrow(() -> service.deleteEntry(patient, "e5"));
        // then
        verify(repo).delete(entry);
    }

    @Test
    void deleteEntry_notFound_throws() {
        when(repo.findById("nope")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteEntry(patient, "nope"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void decryptJournalDTO_shouldDecryptAllFields() throws Exception {
        // prepare DTO
        CoachJournalEntryOutputDTO dto = new CoachJournalEntryOutputDTO();
        dto.setTitle("encT");
        dto.setContent("encC");
        dto.setTags(Set.of("encX", "encY"));

        // stub decrypt static
        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encT", "rawKey")).thenReturn("T");
            crypto.when(() -> CryptographyUtil.decrypt("encC", "rawKey")).thenReturn("C");
            crypto.when(() -> CryptographyUtil.decrypt("encX", "rawKey")).thenReturn("X");
            crypto.when(() -> CryptographyUtil.decrypt("encY", "rawKey")).thenReturn("Y");

            // invoke private
            Method m = JournalEntryService.class.getDeclaredMethod(
                    "decryptJournalDTO", CoachJournalEntryOutputDTO.class, String.class);
            m.setAccessible(true);
            m.invoke(service, dto, "rawKey");

            assertEquals("T", dto.getTitle());
            assertEquals("C", dto.getContent());
            assertEquals(Set.of("X", "Y"), dto.getTags());
        }
    }

    @Test
    void getEntriesForCoach_shouldMapAndDecrypt() {
        // given
        JournalEntry e = new JournalEntry();
        e.setId("e1");
        e.setPatient(patient);
        e.setTitle("encTitle");
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        e.setTags(Set.of("encA", "encB"));
        when(repo.findAllByPatientIdAndSharedWithTherapistTrue("p1"))
                .thenReturn(List.of(e));
        when(patientRepository.getPatientById("p1")).thenReturn(patient);

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.decrypt("encTitle", "rawKey")).thenReturn("Title");
            crypto.when(() -> CryptographyUtil.decrypt("encA", "rawKey")).thenReturn("A");
            crypto.when(() -> CryptographyUtil.decrypt("encB", "rawKey")).thenReturn("B");

            // when
            List<CoachGetAllJournalEntriesDTO> list = service.getEntriesForCoach("p1");

            // then
            assertEquals(1, list.size());
            CoachGetAllJournalEntriesDTO dto = list.get(0);
            assertEquals("e1", dto.getId());
            assertEquals("Title", dto.getTitle());
            assertEquals(Set.of("A", "B"), dto.getTags());
        }
    }

    @Test
    void getOneEntryForCoach_success_shouldDecryptAndReturn() {
        // given
        JournalEntry e = new JournalEntry();
        e.setId("e2");
        e.setPatient(patient);
        e.setTitle("encT");
        e.setContent("encC");
        e.setTags(Set.of("encX"));
        when(repo.findByIdAndSharedWithTherapistTrue("e2"))
                .thenReturn(Optional.of(e));
        when(patientRepository.getPatientById("p1")).thenReturn(patient);

        try (MockedStatic<CryptographyUtil> crypto = mockStatic(CryptographyUtil.class)) {
            crypto.when(() -> CryptographyUtil.decrypt("encKey")).thenReturn("rawKey");
            crypto.when(() -> CryptographyUtil.decrypt("encT", "rawKey")).thenReturn("T");
            crypto.when(() -> CryptographyUtil.decrypt("encC", "rawKey")).thenReturn("C");
            crypto.when(() -> CryptographyUtil.decrypt("encX", "rawKey")).thenReturn("X");

            // when
            CoachJournalEntryOutputDTO dto = service.getOneEntryForCoach("p1", "e2");

            // then
            assertEquals("e2", dto.getId());
            assertEquals("T", dto.getTitle());
            assertEquals("C", dto.getContent());
            assertEquals(Set.of("X"), dto.getTags());
        }
    }

    @Test
    void getOneEntryForCoach_wrongPatient_shouldThrow() {
        // given entry belonging to some other patient
        Patient other = new Patient();
        other.setId("other");
        JournalEntry e = new JournalEntry();
        e.setId("e3");
        e.setPatient(other);
        when(repo.findByIdAndSharedWithTherapistTrue("e3"))
                .thenReturn(Optional.of(e));
        when(patientRepository.getPatientById("p1")).thenReturn(patient);

        // then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getOneEntryForCoach("p1", "e3"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
/*
    @Test
    void getJournalChatbot_shouldReturnOutputDTO_whenValid() {
        // Arrange
        String entryId = "entry123";
        Patient patient = new Patient();
        patient.setId("p1");

        JournalEntryConversation conversation = new JournalEntryConversation();
        JournalEntry entry = new JournalEntry();
        entry.setId(entryId);
        entry.setPatient(patient);
        entry.setJournalEntryConversation(conversation);

        when(repo.findById(entryId)).thenReturn(Optional.of(entry));

        JournalChatbotOutputDTO expected = new JournalChatbotOutputDTO();
        expected.setName("name");

        // Use the real mapper unless you're overriding it with a mock singleton
        JournalChatbotOutputDTO result = service.getJournalChatbot(patient, entryId);

        // Assert
        assertNotNull(result);
        assertEquals("name", result.getName());
    }*/

    @Test
    void getJournalChatbot_shouldThrowNotFound_whenEntryMissing() {
        String entryId = "missing";
        Patient patient = new Patient();
        patient.setId("p1");

        when(repo.findById(entryId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getJournalChatbot(patient, entryId));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Journal Entry not found"));
    }

    @Test
    void getJournalChatbot_shouldThrowNotFound_whenPatientMismatch() {
        String entryId = "entry123";
        Patient caller = new Patient();
        caller.setId("wrong-patient");

        Patient owner = new Patient();
        owner.setId("correct-patient");

        JournalEntry entry = new JournalEntry();
        entry.setId(entryId);
        entry.setPatient(owner);

        when(repo.findById(entryId)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getJournalChatbot(caller, entryId));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Entry not found for that patient"));
    }

    @Test
    void getJournalChatbot_shouldThrowIllegalArgument_whenNoConversation() {
        String entryId = "entry123";
        Patient patient = new Patient();
        patient.setId("p1");

        JournalEntry entry = new JournalEntry();
        entry.setId(entryId);
        entry.setPatient(patient);
        entry.setJournalEntryConversation(null); // <-- no conversation

        when(repo.findById(entryId)).thenReturn(Optional.of(entry));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getJournalChatbot(patient, entryId));

        assertEquals("No conversation found for exercise with ID: entry123", ex.getMessage());
    }
}
