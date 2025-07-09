package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface MessageRepository extends JpaRepository<Message, String> {
    Optional<Message> findById(String id);
    List<Message> findByConversationIdAndInSystemPromptSummaryFalseOrderByCreatedAt(String conversationId);
}
