package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {

    boolean existsById(String id);

    boolean existsByEmail(String email);

    Patient getPatientById(String id);

    Patient getPatientByEmail(String email);
}
