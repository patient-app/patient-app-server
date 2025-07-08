package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseConversationRepository extends JpaRepository<ExerciseConversation, String> {
}
