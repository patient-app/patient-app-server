package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.GeneralConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseInformationRepository extends JpaRepository<ExerciseInformation, String> {
    List<ExerciseInformation> getExerciseInformationByExerciseId(String exerciseId);
}
