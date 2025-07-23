package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.service.LogService;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentChatbotOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;
import ch.uzh.ifi.imrg.patientapp.service.DocumentService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final PatientService patientService;
    private final LogService logService;

    @GetMapping("/patients/documents")
    @ResponseStatus(HttpStatus.OK)
    public List<DocumentOverviewDTO> list(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return documentService.listDocumentsForPatient(loggedInPatient.getId()).stream().map(documentMapper::toOverview)
                .toList();
    }

    @Operation(summary = "Download a patient’s document", responses = {
            @ApiResponse(responseCode = "200", description = "The raw file bytes", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })

    @GetMapping(path = "/patients/documents/{documentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable String documentId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        DocumentDownloadDTO dto = documentService.fetchDocument(loggedInPatient.getId(), documentId);

        MediaType mediaType = MediaType.parseMediaType(dto.getContentType());

        logService.createLog(loggedInPatient.getId(), LogTypes.DOCUMENT_READ, documentId);
        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFilename() + "\"")
                .body(dto.getData());
    }

    @GetMapping("/patients/documents/{documentId}/chatbot")
    public DocumentChatbotOutputDTO getAllMessages(@PathVariable String documentId,
            HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return documentService.getDocumentChatbot(loggedInPatient, documentId);
    }

}
