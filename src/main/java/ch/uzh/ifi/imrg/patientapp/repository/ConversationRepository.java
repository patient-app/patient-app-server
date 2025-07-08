package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Conversation getConversationById(String id);

    Optional<Conversation> findById(String id);
    
    @Query("SELECT c FROM GeneralConversation c WHERE c.patient.id = :patientId")
    List<GeneralConversation> getConversationByPatientId(String patientId);
}
