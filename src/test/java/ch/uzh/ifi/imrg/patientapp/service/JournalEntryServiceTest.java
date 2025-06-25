package ch.uzh.ifi.imrg.patientapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.JournalEntryRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;
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

    @InjectMocks
    private JournalEntryService service;

    private Patient patient;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("p1");
        patient.setPrivateKey("encKey");
        now = LocalDateTime.of(2025, 6, 20, 15, 30);
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
        savedEntity.setCreatedAt(now);
        savedEntity.setUpdatedAt(now);

        when(repo.saveAndFlush(any(JournalEntry.class))).thenReturn(savedEntity);

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
            assertEquals(now, out.getCreatedAt());
            assertEquals(now, out.getUpdatedAt());
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
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
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
            assertEquals(now, dto.getCreatedAt());
            assertEquals(now, dto.getUpdatedAt());
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
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
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
            assertEquals(now, dto.getCreatedAt());
            assertEquals(now, dto.getUpdatedAt());
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
        updated.setCreatedAt(now);
        updated.setUpdatedAt(now.plusDays(1));
        when(repo.saveAndFlush(entry)).thenReturn(updated);

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
            assertEquals(now, out.getCreatedAt());
            assertEquals(now.plusDays(1), out.getUpdatedAt());
        }
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
}
