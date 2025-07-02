package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocumentId;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDocumentRepository extends JpaRepository<PatientDocument, PatientDocumentId> {

    List<PatientDocument> findAllByPatient_Id(String patientId);

    @Query("select pd.document from PatientDocument pd where pd.patient.id = :pid")
    List<Document> findDocumentsByPatientId(@Param("pid") String patientId);

    long countByDocument_Id(String documentId);
}