package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseElement;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
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
    private final ExerciseMapper exerciseMapper;

    public ExerciseService(PatientService patientService, ExerciseRepository exerciseRepository, PatientRepository patientRepository, ExerciseMapper exerciseMapper) {
        this.patientService = patientService;
        this.exerciseRepository = exerciseRepository;
        this.patientRepository = patientRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public List<ExercisesOverviewOutputDTO> getExercisesOverview(Patient patient){
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
    }
    public ExerciseOutputDTO getExercise(String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        return exerciseMapper.exerciseToExerciseOutputDTO(exercise);
    }

    public void createExercise(String patientId, ExerciseInputDTO exerciseInputDTO){
        Exercise exercise = exerciseMapper.createExerciseDTOToExercise(exerciseInputDTO);
        Patient patient = patientRepository.getPatientById(patientId);
        exercise.setPatient(patient);
        //manually set the exercise in the exercise elements
        if (exercise.getExerciseElements() != null) {
            for (ExerciseElement element : exercise.getExerciseElements()) {
                element.setExercise(exercise);
            }
        }
        exerciseRepository.save(exercise);
    }
}
