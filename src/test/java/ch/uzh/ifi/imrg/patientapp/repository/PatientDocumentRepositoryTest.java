package ch.uzh.ifi.imrg.patientapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;

@ActiveProfiles("test")
@DataJpaTest
public class PatientDocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientDocumentRepository patientDocumentRepository;

    /**
     * Verifies that findDocumentsByPatientId only returns the documents
     * linked to the given patient, and does not include others.
     */
    @Test
    void findDocumentsByPatientId_returnsOnlyThatPatientsDocuments() {
        // given two patients
        Patient p1 = new Patient();
        p1.setId("P1");
        p1.setEmail("p1@example.com");
        p1.setPassword("pw1");
        p1.setCoachAccessKey("key1");
        entityManager.persist(p1);

        Patient p2 = new Patient();
        p2.setId("P2");
        p2.setEmail("p2@example.com");
        p2.setPassword("pw2");
        p2.setCoachAccessKey("key2");
        entityManager.persist(p2);

        // and two documents
        Document docA = new Document();
        docA.setFilename("a.txt");
        docA.setContentType("text/plain");
        docA.setData(new byte[] { 1 });
        docA.setSha256("aaa");
        entityManager.persist(docA);

        Document docB = new Document();
        docB.setFilename("b.txt");
        docB.setContentType("text/plain");
        docB.setData(new byte[] { 2 });
        docB.setSha256("bbb");
        entityManager.persist(docB);

        // link p1→docA and p1→docB
        entityManager.persist(new PatientDocument(p1, docA));
        entityManager.persist(new PatientDocument(p1, docB));
        // link p2→docB only
        entityManager.persist(new PatientDocument(p2, docB));

        entityManager.flush();
        entityManager.clear();

        // when
        List<Document> p1Docs = patientDocumentRepository.findDocumentsByPatientId("P1");
        List<Document> p2Docs = patientDocumentRepository.findDocumentsByPatientId("P2");

        // then
        assertThat(p1Docs)
                .extracting(Document::getId)
                .containsExactlyInAnyOrder(docA.getId(), docB.getId());

        assertThat(p2Docs)
                .extracting(Document::getId)
                .containsExactly(docB.getId());
    }

    /**
     * Verifies that countByDocument_Id returns the correct number of
     * PatientDocument links for a given document ID.
     */
    @Test
    void countByDocumentId_countsAllLinks() {
        // given one document
        Document doc = new Document();
        doc.setFilename("shared.pdf");
        doc.setContentType("application/pdf");
        doc.setData(new byte[] { 9 });
        doc.setSha256("ccc");
        entityManager.persist(doc);

        // and two patients both linked to it
        Patient x = new Patient();
        x.setId("X");
        x.setEmail("x@example.com");
        x.setPassword("pw");
        x.setCoachAccessKey("keyX");
        entityManager.persist(x);

        Patient y = new Patient();
        y.setId("Y");
        y.setEmail("y@example.com");
        y.setPassword("pw");
        y.setCoachAccessKey("keyY");
        entityManager.persist(y);

        entityManager.persist(new PatientDocument(x, doc));
        entityManager.persist(new PatientDocument(y, doc));

        entityManager.flush();
        entityManager.clear();

        // when / then
        long count = patientDocumentRepository.countByDocument_Id(doc.getId());
        assertThat(count).isEqualTo(2);

        // and for a nonexistent document
        long zero = patientDocumentRepository.countByDocument_Id("does-not-exist");
        assertThat(zero).isEqualTo(0);
    }
}
