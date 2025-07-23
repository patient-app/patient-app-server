package ch.uzh.ifi.imrg.patientapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentChatbotOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;
import ch.uzh.ifi.imrg.patientapp.service.DocumentService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private DocumentService documentService;
        @Mock
        private DocumentMapper documentMapper;
        @Mock
        private PatientService patientService;
        @Mock
        private LogService logService;

        @InjectMocks
        private DocumentController controller;

        private Patient mockPatient;

        @BeforeEach
        void setUp() {
                // configure Jackson for Instant
                objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(controller)
                                .setMessageConverters(
                                                new MappingJackson2HttpMessageConverter(objectMapper),
                                                new ByteArrayHttpMessageConverter())
                                .build();

                mockPatient = new Patient();
                mockPatient.setId("p123");

                // any(HttpServletRequest) covers both endpoints
                when(patientService.getCurrentlyLoggedInPatient(any(HttpServletRequest.class)))
                                .thenReturn(mockPatient);
        }

        @Test
        void list_shouldReturnAllDocumentOverviews() throws Exception {
                // prepare two documents
                Document doc1 = new Document();
                doc1.setId("dA");
                Document doc2 = new Document();
                doc2.setId("dB");
                when(documentService.listDocumentsForPatient("p123"))
                                .thenReturn(List.of(doc1, doc2));

                // map them to DTOs
                DocumentOverviewDTO dto1 = new DocumentOverviewDTO();
                dto1.setId("dA");
                dto1.setFilename("a.pdf");
                dto1.setContentType("application/pdf");
                dto1.setUploadedAt(Instant.parse("2025-06-30T09:00:00Z"));

                DocumentOverviewDTO dto2 = new DocumentOverviewDTO();
                dto2.setId("dB");
                dto2.setFilename("b.png");
                dto2.setContentType("image/png");
                dto2.setUploadedAt(Instant.parse("2025-06-30T10:00:00Z"));

                when(documentMapper.toOverview(doc1)).thenReturn(dto1);
                when(documentMapper.toOverview(doc2)).thenReturn(dto2);

                mockMvc.perform(get("/patients/documents")
                                .requestAttr("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                                controller)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value("dA"))
                                .andExpect(jsonPath("$[0].filename").value("a.pdf"))
                                .andExpect(jsonPath("$[0].uploadedAt").value("2025-06-30T09:00:00Z"))
                                .andExpect(jsonPath("$[1].id").value("dB"))
                                .andExpect(jsonPath("$[1].filename").value("b.png"));
        }

        @Test
        void download_shouldReturnFileBytesAndHeaders() throws Exception {
                String documentId = "dX";
                byte[] data = { 10, 20, 30 };

                DocumentDownloadDTO downloadDto = new DocumentDownloadDTO(
                                "report.docx",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", data);
                when(documentService.fetchDocument("p123", documentId))
                                .thenReturn(downloadDto);
                doNothing().when(logService).createLog(eq("p123"), any(LogTypes.class), eq("dX"), "");
                mockMvc.perform(get("/patients/documents/{documentId}", documentId)
                                .accept(MediaType.ALL))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Disposition",
                                                "attachment; filename=\"report.docx\""))
                                .andExpect(content()
                                                .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                                .andExpect(content().bytes(data));
        }

        @Test
        void getChatbot_shouldReturnChatbotOutput() throws Exception {
                // 1) Arrange
                String documentId = "docC";

                DocumentChatbotOutputDTO chatbotDto = new DocumentChatbotOutputDTO();
                // populate any fields you want to assert on:
                chatbotDto.setId(documentId);
                chatbotDto.setName("someName");

                when(documentService.getDocumentChatbot(mockPatient, documentId))
                                .thenReturn(chatbotDto);

                // 2) Act & 3) Assert
                mockMvc.perform(get("/patients/documents/{documentId}/chatbot", documentId)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                // compare the full JSON payload to what Jackson produces for your DTO
                                .andExpect(content().json(objectMapper.writeValueAsString(chatbotDto)));
        }
}
