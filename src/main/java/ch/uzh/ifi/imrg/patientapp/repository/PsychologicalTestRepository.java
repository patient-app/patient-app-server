package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.StoredExerciseFile;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PsychologicalTestRepository extends JpaRepository<PsychologicalTest, String> {
}
