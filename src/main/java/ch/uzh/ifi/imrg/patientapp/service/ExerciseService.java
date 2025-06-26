package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseElement;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseInformation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.StoredExerciseFile;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseInformationRepository;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.repository.StoredExerciseFileRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseMediaOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExerciseService {
    private final PatientService patientService;
    private final ExerciseRepository exerciseRepository;
    private final PatientRepository patientRepository;
    private final ExerciseMapper exerciseMapper;
    private final StoredExerciseFileRepository storedExerciseFileRepository;
    private final ExerciseInformationRepository exerciseInformationRepository;

    public ExerciseService(PatientService patientService, ExerciseRepository exerciseRepository, PatientRepository patientRepository, ExerciseMapper exerciseMapper, StoredExerciseFileRepository storedExerciseFileRepository, ExerciseInformationRepository exerciseInformationRepository) {
        this.patientService = patientService;
        this.exerciseRepository = exerciseRepository;
        this.patientRepository = patientRepository;
        this.exerciseMapper = exerciseMapper;
        this.storedExerciseFileRepository = storedExerciseFileRepository;
        this.exerciseInformationRepository = exerciseInformationRepository;
    }

    public List<ExercisesOverviewOutputDTO>getAllExercisesForCoach(String patientId){
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("No patient found with ID: " + patientId);
        }
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
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

    public ExerciseMediaOutputDTO getExerciseMedia(Patient patient, String exerciseId, String mediaId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        Optional <StoredExerciseFile> optionalStoredExerciseFile = storedExerciseFileRepository.findById(mediaId);
        if (optionalStoredExerciseFile.isEmpty()) {
            throw new IllegalArgumentException("No media found with ID: " + mediaId);
        }
        StoredExerciseFile storedExerciseFile = optionalStoredExerciseFile.get();
        return exerciseMapper.storedExerciseFileToExerciseMediaOutputDTO(storedExerciseFile);
    }

    public void createExercise(String patientId, ExerciseInputDTO exerciseInputDTO){
        Exercise exercise = exerciseMapper.exerciseInputDTOToExercise(exerciseInputDTO);
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
    public void updateExercise(String patientId, String exerciseId, ExerciseInputDTO exerciseInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }

        exerciseMapper.updateExerciseFromInputDTO(exerciseInputDTO, exercise);
        exerciseRepository.save(exercise);
    }

    public void deleteExercise(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        exerciseRepository.delete(exercise);
    }

    public void putExerciseFeedback(Patient patient, String exerciseId, ExerciseInformationInputDTO exerciseInformationInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        ExerciseInformation exerciseInformation = exerciseMapper.exerciseInformationInputDTOToExerciseInformation(exerciseInformationInputDTO);
        exerciseInformation.setExercise(exercise);
        exerciseInformationRepository.save(exerciseInformation);
    }
    public List<ExerciseInformationOutputDTO> getExerciseInformation(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        List<ExerciseInformation> exerciseInformations = exerciseInformationRepository.getExerciseInformationByExerciseId(exercise.getId());
        return exerciseMapper.exerciseInformationsToExerciseInformationOutputDTOs(exerciseInformations);
    }

}

