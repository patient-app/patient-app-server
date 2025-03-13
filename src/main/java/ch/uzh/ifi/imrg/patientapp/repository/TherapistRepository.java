package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("therapistRepository")
public interface TherapistRepository extends JpaRepository<Therapist, String> {

    boolean existsByEmail(String email);

    Therapist getTherapistByEmail(String email);
}
