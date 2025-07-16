package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseComponentRepository extends JpaRepository<ExerciseComponent, String> {

    ExerciseComponent getExerciseComponentById(String id);
}
