package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Conversation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseInformationRepository extends JpaRepository<ExerciseInformation, String> {
}
