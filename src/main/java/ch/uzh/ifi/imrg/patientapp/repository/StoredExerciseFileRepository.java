package ch.uzh.ifi.imrg.patientapp.repository;


import ch.uzh.ifi.imrg.patientapp.entity.Exercise.StoredExerciseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredExerciseFileRepository extends JpaRepository<StoredExerciseFile, String> {
    Optional<StoredExerciseFile> findById(String id);
}
