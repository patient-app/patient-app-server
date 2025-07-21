package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {
    List<Exercise> getExercisesByPatientId(String patientId);
    Exercise getExerciseById(String exerciseId);


    @Query("SELECT e FROM Exercise e " +
            "WHERE e.isPaused = false " +
            "AND :now BETWEEN e.exerciseStart AND e.exerciseEnd")
    List<Exercise> findAllActiveExercisesWithinTime(@Param("now") Instant now);
}
