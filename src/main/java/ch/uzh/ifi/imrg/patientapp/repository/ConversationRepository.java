package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<GeneralConversation, String> {
    GeneralConversation getConversationById(String id);

    Optional<GeneralConversation> findById(String id);

    List<GeneralConversation> getConversationByPatientId(String patientId);
}
