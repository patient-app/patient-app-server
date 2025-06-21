package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.Exercise.CreateExerciseDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ExerciseService {
    private final PatientService patientService;
    private final ExerciseRepository exerciseRepository;
    private final PatientRepository patientRepository;

    public ExerciseService(PatientService patientService, ExerciseRepository exerciseRepository, PatientRepository patientRepository) {
        this.patientService = patientService;
        this.exerciseRepository = exerciseRepository;
        this.patientRepository = patientRepository;
    }

    public List<ExercisesOverviewOutputDTO> getExercisesOverview(Patient patient){
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return ExerciseMapper.INSTANCE.exercisesToExerciseOverviewOutputDTOs(exercises);
    }
    public ExerciseOutputDTO
    public void createExercise(String patientId, CreateExerciseDTO createExerciseDTO){
        Exercise exercise = ExerciseMapper.INSTANCE.createExerciseDTOToExercise(createExerciseDTO);
        Patient patient = patientRepository.getPatientById(patientId);
        exercise.setPatient(patient);
        exerciseRepository.save(exercise);
    }
}
