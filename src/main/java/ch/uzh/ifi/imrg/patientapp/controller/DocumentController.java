package ch.uzh.ifi.imrg.patientapp.controller;

import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;
import ch.uzh.ifi.imrg.patientapp.service.DocumentService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
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

    @GetMapping("/patients/documents")
    @ResponseStatus(HttpStatus.OK)
    public List<DocumentOverviewDTO> list(HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        return documentService.listDocumentsForPatient(loggedInPatient.getId()).stream().map(documentMapper::toOverview)
                .toList();
    }

    @GetMapping("/patients/documents/{documentId}")
    public ResponseEntity<byte[]> download(@PathVariable String documentId, HttpServletRequest httpServletRequest) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);

        DocumentDownloadDTO dto = documentService.fetchDocument(loggedInPatient.getId(), documentId);

        MediaType mediaType = MediaType.parseMediaType(dto.getContentType());

        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFilename() + "\"")
                .body(dto.getData());
    }

}
