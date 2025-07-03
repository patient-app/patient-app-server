package ch.uzh.ifi.imrg.patientapp.entity.Document;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("DOCUMENT")
@Table(name = "document_conversations")
public class DocumentConversation extends Conversation {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "pd_patient_id", referencedColumnName = "patient_id", nullable = false),
            @JoinColumn(name = "pd_document_id", referencedColumnName = "document_id", nullable = false)
    })
    private PatientDocument patientDocument;

    @Column(name = "document_context", unique = false)
    private String documentContext;

    public DocumentConversation(PatientDocument patientDocument) {
        this.patient = patientDocument.getPatient();
        this.patientDocument = patientDocument;
    }

}
