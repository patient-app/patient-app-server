package ch.uzh.ifi.imrg.patientapp.repository;

import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsychologicalTestRepository extends JpaRepository<PsychologicalTest, String> {

    List<PsychologicalTestNameOutputDTO> findAllByPatientId(@Param("patientId") String patientId);

    @Query("SELECT new ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO(t.name) " +
            "FROM PsychologicalTest t " +
            "WHERE t.patient.id = :patientId AND t.name = :name")
    List<PsychologicalTestOutputDTO> findAllByPatientIdAndName(@Param("patientId") String patientId,
                                                               @Param("name") String name);

}
