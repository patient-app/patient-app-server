package ch.uzh.ifi.imrg.patientapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    Optional<Document> findBySha256AndFilename(String sha256, String filename);

}
