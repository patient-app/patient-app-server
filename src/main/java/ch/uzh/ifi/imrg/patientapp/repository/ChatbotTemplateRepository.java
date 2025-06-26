package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("chatbotTemplateRepository")
public interface ChatbotTemplateRepository extends JpaRepository<ChatbotTemplate, String> {
    List<ChatbotTemplate> findByPatientId(String patientId);

}
