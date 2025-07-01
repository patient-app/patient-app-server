package ch.uzh.ifi.imrg.patientapp.entity.Document;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PatientDocumentId implements Serializable {
    private String patientId;
    private String documentId;

    public PatientDocumentId(String patientId, String documentId) {
        this.patientId = patientId;
        this.documentId = documentId;
    }
}
