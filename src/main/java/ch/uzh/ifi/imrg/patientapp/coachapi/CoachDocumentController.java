package ch.uzh.ifi.imrg.patientapp.coachapi;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;
import ch.uzh.ifi.imrg.patientapp.service.DocumentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
public class CoachDocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;

    @PostMapping(path = "/coach/patients/{patientId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public DocumentOverviewDTO uploadAndShare(@PathVariable String patientId,
            @RequestParam("file") MultipartFile file) {

        Document doc = documentService.uploadAndShare(patientId, file);

        return documentMapper.toOverview(doc);
    }

    @GetMapping("coach/patients/{patientId}/documents")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<DocumentOverviewDTO> list(@PathVariable String patientId) {
        return documentService.listDocumentsForPatient(patientId).stream().map(documentMapper::toOverview).toList();
    }

    @GetMapping("coach/patients/{patientId}/documents/{documentId}")
    @SecurityRequirement(name = "X-Coach-Key")
    public ResponseEntity<byte[]> download(@PathVariable String patientId, @PathVariable String documentId) {
        DocumentDownloadDTO dto = documentService.fetchDocument(patientId, documentId);

        MediaType mediaType = MediaType.parseMediaType(dto.getContentType());

        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFilename() + "\"")
                .body(dto.getData());
    }

    @DeleteMapping("/coach/patients/{patientId}/documents/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deleteDocument(@PathVariable String patientId, @PathVariable String documentId) {
        documentService.removeDocumentForPatient(patientId, documentId);
    }

}
