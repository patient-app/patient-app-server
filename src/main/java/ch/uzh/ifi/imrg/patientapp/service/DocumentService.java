package ch.uzh.ifi.imrg.patientapp.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.entity.Document.DocumentConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocumentId;
import ch.uzh.ifi.imrg.patientapp.repository.DocumentRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientDocumentRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentChatbotOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.DocumentMapper;
import ch.uzh.ifi.imrg.patientapp.utils.DocumentUtil;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final MessageDigest digest;
    private final DocumentMapper documentMapper;
    private final PromptBuilderService promptBuilderService;

    public DocumentService(DocumentRepository documentRepository, PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository, DocumentMapper documentMapper,
            PromptBuilderService promptBuilderService) {
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        this.documentMapper = documentMapper;
        this.promptBuilderService = promptBuilderService;
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

        PatientDocument patientDocument = new PatientDocument(patient, document);
        patientDocumentRepository.save(patientDocument);

        DocumentUtil.ExtractionResult result = DocumentUtil.extractTextResult(patientDocument);

        if (result.isHumanReadable()) {
            patientDocument.getConversation().setSystemPrompt(
                    promptBuilderService.getDocumentSystemPrompt(patient.getChatbotTemplate(), result.getText()));
            patientDocumentRepository.save(patientDocument);
        } else {
            patientDocument.getConversation().setSystemPrompt(
                    promptBuilderService.getDocumentSystemPrompt(patient.getChatbotTemplate(),
                            "The document does not have readable text"));
            patientDocumentRepository.save(patientDocument);
        }

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

    public void removeAllDocumentsForPatient(String patientId) {
        List<Document> docs = listDocumentsForPatient(patientId);

        for (Document doc : docs) {
            removeDocumentForPatient(patientId, doc.getId());
        }
    }

    public DocumentChatbotOutputDTO getDocumentChatbot(Patient patient, String documentId) {
        PatientDocumentId pk = new PatientDocumentId(patient.getId(), documentId);
        PatientDocument patientDocument = patientDocumentRepository.findById(pk)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No PatientDocument for patient=" + patient.getId() + " and document=" + documentId));

        DocumentConversation conversation = patientDocument.getConversation();

        if (conversation == null) {
            throw new IllegalArgumentException("No conversation found for exercise with ID: " + documentId);
        }

        return documentMapper.documentConversationToExcerciseChatbotOutputDTO(conversation);
    }
}
