package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseCompletionInformation;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentResultInputDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseInformationRepository extends JpaRepository<ExerciseCompletionInformation, String> {
    List<ExerciseCompletionInformation> getExerciseInformationByExerciseId(String exerciseId);
    ExerciseCompletionInformation getExerciseCompletionInformationById(String exerciseExecutionId);
}
