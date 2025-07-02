package ch.uzh.ifi.imrg.patientapp.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final MessageDigest digest;

    public DocumentService(DocumentRepository documentRepository, PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository) {
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public Document uploadAndShare(String patientId, MultipartFile file) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file content", e);
        }

        byte[] hashBytes = digest.digest(bytes);
        String sha256 = HexFormat.of().formatHex(hashBytes);

        Document document = documentRepository.findBySha256(sha256).orElseGet(() -> {
            Document d = new Document();
            d.setFilename(file.getOriginalFilename());
            d.setContentType(file.getContentType());
            d.setData(bytes);
            d.setSha256(sha256);
            return documentRepository.save(d);
        });

        patientDocumentRepository.save(new PatientDocument(patient, document));

        return document;

    }

    public List<Document> listDocumentsForPatient(String patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return patientDocumentRepository.findDocumentsByPatientId(patient.getId());
    }

    public DocumentDownloadDTO fetchDocument(String patientId, String documentId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        PatientDocument pd = patientDocumentRepository.findById(new PatientDocumentId(patient.getId(), documentId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Access denied or document not shared"));

        Document doc = pd.getDocument();
        return new DocumentDownloadDTO(doc.getFilename(), doc.getContentType(), doc.getData());
    }

    public void removeDocumentForPatient(String patientId, String documentId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Patient not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found"));

        PatientDocumentId pdId = new PatientDocumentId(patient.getId(), document.getId());

        patientDocumentRepository.deleteById(pdId);

        long remaining = patientDocumentRepository.countByDocument_Id(documentId);
        if (remaining == 0) {
            documentRepository.deleteById(documentId);
        }

    }
}
