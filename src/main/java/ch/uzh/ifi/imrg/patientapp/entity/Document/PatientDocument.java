package ch.uzh.ifi.imrg.patientapp.entity.Document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;

@Entity
@Table(name = "patient_documents")
@Getter
@Setter
@NoArgsConstructor
public class PatientDocument {

    @EmbeddedId
    private PatientDocumentId id = new PatientDocumentId();

    @ManyToOne(optional = false)
    @MapsId("patientId")
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @MapsId("documentId")
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant grantedAt;

    public PatientDocument(Patient patient, Document document) {
        this.patient = patient;
        this.document = document;
        this.id.setPatientId(patient.getId());
        this.id.setDocumentId(document.getId());
    }
}
