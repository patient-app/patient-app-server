package ch.uzh.ifi.imrg.patientapp.coachapi;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.service.DocumentService;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@ExtendWith(MockitoExtension.class)
class CoachDocumentControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private DocumentService documentService;

        @Mock
        private DocumentMapper documentMapper;

        @InjectMocks
        private CoachDocumentController controller;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(controller)
                                .setMessageConverters(
                                                new MappingJackson2HttpMessageConverter(objectMapper),
                                                new ByteArrayHttpMessageConverter())
                                .build();
        }

        @Test
        void uploadAndShare_shouldReturnCreatedOverview() throws Exception {
                String patientId = "p1";
                MockMultipartFile file = new MockMultipartFile(
                                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

                // stub service and mapper
                Document doc = new Document();
                doc.setId("d1");
                when(documentService.uploadAndShare(eq(patientId), any())).thenReturn(doc);

                DocumentOverviewDTO dto = new DocumentOverviewDTO();
                dto.setId("d1");
                dto.setFilename("test.txt");
                dto.setContentType(MediaType.TEXT_PLAIN_VALUE);
                dto.setUploadedAt(Instant.parse("2025-07-01T12:00:00Z"));
                when(documentMapper.toOverview(doc)).thenReturn(dto);

                mockMvc.perform(multipart("/coach/patients/{patientId}/documents", patientId)
                                .file(file)
                                .header("X-Coach-Key", "key"))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value("d1"))
                                .andExpect(jsonPath("$.filename").value("test.txt"))
                                .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
                                .andExpect(jsonPath("$.uploadedAt").value("2025-07-01T12:00:00Z"));
        }

        @Test
        void list_shouldReturnAllOverviews() throws Exception {
                String patientId = "p2";

                // two sample documents
                Document doc1 = new Document();
                doc1.setId("dA");
                Document doc2 = new Document();
                doc2.setId("dB");
                when(documentService.listDocumentsForPatient(patientId))
                                .thenReturn(List.of(doc1, doc2));

                DocumentOverviewDTO dto1 = new DocumentOverviewDTO();
                dto1.setId("dA");
                dto1.setFilename("a.pdf");
                dto1.setContentType("application/pdf");
                dto1.setUploadedAt(Instant.parse("2025-07-01T08:00:00Z"));
                DocumentOverviewDTO dto2 = new DocumentOverviewDTO();
                dto2.setId("dB");
                dto2.setFilename("b.png");
                dto2.setContentType("image/png");
                dto2.setUploadedAt(Instant.parse("2025-07-02T09:30:00Z"));

                when(documentMapper.toOverview(doc1)).thenReturn(dto1);
                when(documentMapper.toOverview(doc2)).thenReturn(dto2);

                mockMvc.perform(get("/coach/patients/{patientId}/documents", patientId)
                                .header("X-Coach-Key", "key"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value("dA"))
                                .andExpect(jsonPath("$[0].filename").value("a.pdf"))
                                .andExpect(jsonPath("$[1].id").value("dB"))
                                .andExpect(jsonPath("$[1].filename").value("b.png"));
        }

        @Test
        void download_shouldReturnFileBytesAndHeaders() throws Exception {
                String patientId = "p3";
                String documentId = "dX";
                byte[] data = { 1, 2, 3, 4 };

                DocumentDownloadDTO dto = new DocumentDownloadDTO("file.bin", "application/octet-stream", data);
                when(documentService.fetchDocument(patientId, documentId)).thenReturn(dto);

                mockMvc.perform(get("/coach/patients/{patientId}/documents/{documentId}", patientId, documentId)
                                .header("X-Coach-Key", "key"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"file.bin\""))
                                .andExpect(content().contentType("application/octet-stream"))
                                .andExpect(content().bytes(data));
        }

        @Test
        void deleteDocument_shouldReturnNoContent() throws Exception {
                String patientId = "p4";
                String documentId = "dY";

                doNothing().when(documentService).removeDocumentForPatient(patientId, documentId);

                mockMvc.perform(delete("/coach/patients/{patientId}/documents/{documentId}", patientId, documentId)
                                .header("X-Coach-Key", "key"))
                                .andExpect(status().isNoContent());
        }
}
