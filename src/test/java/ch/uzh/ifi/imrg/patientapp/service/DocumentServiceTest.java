package ch.uzh.ifi.imrg.patientapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocumentId;
import ch.uzh.ifi.imrg.patientapp.repository.DocumentRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientDocumentRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import ch.uzh.ifi.imrg.patientapp.utils.DocumentUtil;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

        @Mock
        private DocumentRepository documentRepository;
        @Mock
        private PatientRepository patientRepository;
        @Mock
        private PatientDocumentRepository patientDocumentRepository;

        @Mock
        private PromptBuilderService promptBuilderService;

        @Mock
        private DocumentUtil documentUtil;

        @InjectMocks
        private DocumentService service;

        private Patient patient;
        private byte[] content = "hello".getBytes();
        private String sha256;

        @BeforeEach
        void setUp() throws Exception {
                patient = new Patient();
                patient.setId("p1");
                Mockito.lenient().when(patientRepository.findById("p1"))
                                .thenReturn(Optional.of(patient));

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                sha256 = HexFormat.of().formatHex(digest.digest(content));
        }

        @Test
        void uploadAndShare_createsNewDocumentAndShares() throws Exception {
                MultipartFile file = new MockMultipartFile("file", "foo.txt",
                                "text/plain", content);

                when(documentRepository.findBySha256(sha256))
                                .thenReturn(Optional.empty());
                Document saved = new Document();
                saved.setId("d1");
                saved.setFilename("foo.txt");
                saved.setContentType("text/plain");
                saved.setData(content);
                saved.setSha256(sha256);
                when(documentRepository.save(any(Document.class)))
                                .thenReturn(saved);

                Document result = service.uploadAndShare("p1", file);

                assertEquals("d1", result.getId());
                verify(patientDocumentRepository, times(2))
                                .save(argThat(pd -> pd.getPatient().equals(patient) &&
                                                pd.getDocument().equals(saved)));
        }

        @Test
        void uploadAndShare_reusesExistingDocument() throws Exception {
                MultipartFile file = new MockMultipartFile("file", "bar.pdf",
                                "application/pdf", content);

                Document existing = new Document();
                existing.setId("d2");
                existing.setSha256(sha256);
                when(documentRepository.findBySha256(sha256))
                                .thenReturn(Optional.of(existing));

                try (MockedStatic<DocumentUtil> mocked = mockStatic(DocumentUtil.class)) {

                        mocked.when(() -> DocumentUtil.extractTextResult(any(PatientDocument.class)))
                                        .thenReturn(new DocumentUtil.ExtractionResult("ignored", false));

                        Document result = service.uploadAndShare("p1", file);

                        assertSame(existing, result);
                        verify(documentRepository, never()).save(any());
                        verify(patientDocumentRepository, times(2)).save(any(PatientDocument.class));
                }
        }

        @Test
        void uploadAndShare_patientNotFound_throws404() {
                when(patientRepository.findById("nope"))
                                .thenReturn(Optional.empty());

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.uploadAndShare("nope", mock(MultipartFile.class)));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void uploadAndShare_ioError_throws400() throws Exception {
                MultipartFile file = mock(MultipartFile.class);
                when(patientRepository.findById("p1"))
                                .thenReturn(Optional.of(patient));
                when(file.getBytes()).thenThrow(new IOException("boom"));

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.uploadAndShare("p1", file));
                assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }

        @Test
        void listDocumentsForPatient_returnsList() {
                Document d1 = new Document();
                d1.setId("d1");
                Document d2 = new Document();
                d2.setId("d2");
                when(patientDocumentRepository.findDocumentsByPatientId("p1"))
                                .thenReturn(List.of(d1, d2));

                List<Document> list = service.listDocumentsForPatient("p1");
                assertEquals(2, list.size());
        }

        @Test
        void listDocumentsForPatient_patientNotFound_throws404() {
                when(patientRepository.findById("nope"))
                                .thenReturn(Optional.empty());
                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.listDocumentsForPatient("nope"));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void fetchDocument_returnsDTO() {
                Document doc = new Document();
                doc.setId("d1");
                doc.setFilename("f");
                doc.setContentType("ct");
                doc.setData(new byte[] { 1, 2, 3 });
                PatientDocument pd = new PatientDocument(patient, doc);

                when(patientDocumentRepository.findById(
                                new PatientDocumentId("p1", "d1")))
                                .thenReturn(Optional.of(pd));

                DocumentDownloadDTO dto = service.fetchDocument("p1", "d1");
                assertEquals("f", dto.getFilename());
                assertArrayEquals(new byte[] { 1, 2, 3 }, dto.getData());
        }

        @Test
        void fetchDocument_patientNotFound_throws404() {
                when(patientRepository.findById("nope"))
                                .thenReturn(Optional.empty());
                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.fetchDocument("nope", "d1"));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void fetchDocument_notShared_throws404() {
                when(patientRepository.findById("p1"))
                                .thenReturn(Optional.of(patient));
                when(patientDocumentRepository.findById(any()))
                                .thenReturn(Optional.empty());

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.fetchDocument("p1", "d1"));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void removeDocumentForPatient_deletesLinkAndOrphanedDoc() {
                Document doc = new Document();
                doc.setId("d1");
                when(documentRepository.findById("d1"))
                                .thenReturn(Optional.of(doc));
                when(patientDocumentRepository.countByDocument_Id("d1"))
                                .thenReturn(0L);

                assertDoesNotThrow(() -> service.removeDocumentForPatient("p1", "d1"));

                verify(patientDocumentRepository)
                                .deleteById(new PatientDocumentId("p1", "d1"));
                verify(documentRepository)
                                .deleteById("d1");
        }

        @Test
        void removeDocumentForPatient_deletesLinkButKeepsSharedDoc() {
                Document doc = new Document();
                doc.setId("d2");
                when(documentRepository.findById("d2"))
                                .thenReturn(Optional.of(doc));
                when(patientDocumentRepository.countByDocument_Id("d2"))
                                .thenReturn(3L);

                service.removeDocumentForPatient("p1", "d2");

                verify(patientDocumentRepository)
                                .deleteById(new PatientDocumentId("p1", "d2"));
                verify(documentRepository, never())
                                .deleteById("d2");
        }

        @Test
        void removeDocumentForPatient_patientNotFound_throws404() {
                when(patientRepository.findById("nope"))
                                .thenReturn(Optional.empty());
                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.removeDocumentForPatient("nope", "d1"));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void removeDocumentForPatient_docNotFound_throws404() {
                when(patientRepository.findById("p1"))
                                .thenReturn(Optional.of(patient));
                when(documentRepository.findById("nope"))
                                .thenReturn(Optional.empty());

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> service.removeDocumentForPatient("p1", "nope"));
                assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void removeAllDocumentsForPatient_invokesRemoveEach() {
                DocumentService spySvc = Mockito.spy(service);
                Document d1 = new Document();
                d1.setId("d1");
                Document d2 = new Document();
                d2.setId("d2");
                doReturn(List.of(d1, d2))
                                .when(spySvc).listDocumentsForPatient("p1");

                doNothing().when(spySvc).removeDocumentForPatient(anyString(), anyString());

                spySvc.removeAllDocumentsForPatient("p1");

                verify(spySvc).removeDocumentForPatient("p1", "d1");
                verify(spySvc).removeDocumentForPatient("p1", "d2");
        }
}
