package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseCompletionInformation;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseComponent;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseComponentMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseComponentRepository exerciseComponentRepository;
    private final PatientRepository patientRepository;
    private final StoredExerciseFileRepository storedExerciseFileRepository;
    private final ExerciseInformationRepository exerciseInformationRepository;
    private final ExerciseConversationRepository exerciseConversationRepository;
    private final PromptBuilderService promptBuilderService;
    private final AuthorizationService authorizationService;
    private final ChatbotTemplateRepository chatbotTemplateRepository;
    private final ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseComponentRepository exerciseComponentRepository, PatientRepository patientRepository, StoredExerciseFileRepository storedExerciseFileRepository, ExerciseInformationRepository exerciseInformationRepository, ExerciseConversationRepository exerciseConversationRepository, PromptBuilderService promptBuilderService, AuthorizationService authorizationService, ChatbotTemplateRepository chatbotTemplateRepository, ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseComponentRepository = exerciseComponentRepository;
        this.patientRepository = patientRepository;
        this.storedExerciseFileRepository = storedExerciseFileRepository;
        this.exerciseInformationRepository = exerciseInformationRepository;
        this.exerciseConversationRepository = exerciseConversationRepository;
        this.promptBuilderService = promptBuilderService;
        this.authorizationService = authorizationService;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public List<ExercisesOverviewOutputDTO>getAllExercisesForCoach(String patientId){
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("No patient found with ID: " + patientId);
        }
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
    }
    public List<ExerciseComponentOverviewOutputDTO> getAllExercisesComponentsOfAnExerciseForCoach(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");
        return ExerciseComponentMapper.INSTANCE.exerciseComponentsToExerciseComponentsOverviewOutputDTOs(exercise.getExerciseComponents());
    }

    public List<ExercisesOverviewOutputDTO> getExercisesOverview(Patient patient){
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
    }

    public ExerciseOutputDTO getExercise(String exerciseId, Patient patient) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
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
        if (exercise.getExerciseComponents() != null) {
            for (ExerciseComponent exerciseComponent : exercise.getExerciseComponents()) {
                exerciseComponent.setExercise(exercise);
            }
        }

        ChatbotTemplate chatbotTemplate = chatbotTemplateRepository.findByPatientId(patientId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chatbot template found for patient with ID: " + patientId));

        ExerciseConversation exerciseConversation = new ExerciseConversation();
        exerciseConversation.setPatient(patient);
        exerciseConversation.setSystemPrompt(promptBuilderService.getSystemPrompt(chatbotTemplate,exerciseInputDTO.getExerciseExplanation()));
        exerciseConversation.setConversationName(exerciseInputDTO.getExerciseTitle()+ " - chatbot");
        exerciseConversationRepository.save(exerciseConversation);

        exercise.setExerciseConversation(exerciseConversation);
        exerciseRepository.save(exercise);

    }
    public void createExerciseComponent(String patientId, String exerciseId, ExerciseComponentInputDTO exerciseComponentInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");
        ExerciseComponent exerciseComponent = ExerciseComponentMapper.INSTANCE.exerciseComponentInputDTOToExerciseComponent(exerciseComponentInputDTO);
        exerciseComponent.setExercise(exercise);
        exercise.getExerciseComponents().add(exerciseComponent);
        exerciseRepository.save(exercise);
    }

    public ExerciseChatbotOutputDTO getExerciseChatbot(String exerciseId, Patient patient) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        ExerciseConversation conversation = exercise.getExerciseConversation();
        if (conversation == null) {
            throw new IllegalArgumentException("No conversation found for exercise with ID: " + exerciseId);
        }
        return exerciseMapper.exerciseConversationToExerciseChatbotOutputDTO(conversation);
    }

    public void updateExercise(String patientId, String exerciseId, ExerciseInputDTO exerciseInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");

        exerciseMapper.updateExerciseFromInputDTO(exerciseInputDTO, exercise);
        exerciseRepository.save(exercise);
    }

    public void updateExerciseComponent(String patientId, String exerciseId, String exerciseComponentId, ExerciseComponentInputDTO exerciseComponentInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");

        ExerciseComponent exerciseComponentFromRepository = exerciseComponentRepository.getExerciseComponentById(exerciseComponentId);
        if (exerciseComponentFromRepository == null) {
            throw new IllegalArgumentException("No exercise component found with ID: " + exerciseComponentId);
        }

        ExerciseComponentMapper.INSTANCE.updateExerciseComponentFromExerciseComponentInputDTO(exerciseComponentInputDTO, exerciseComponentFromRepository);
        exerciseComponentRepository.save(exerciseComponentFromRepository);
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

    public void deleteExerciseComponent(String patientId, String exerciseId, String exerciseComponentId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");

        ExerciseComponent exerciseComponent = exerciseComponentRepository.getExerciseComponentById(exerciseComponentId);
        if (exerciseComponent == null) {
            throw new IllegalArgumentException("No exercise component found with ID: " + exerciseComponentId);
        }
        exercise.getExerciseComponents().remove(exerciseComponent);
        exerciseComponentRepository.delete(exerciseComponent);
    }

    public void putExerciseFeedback(Patient patient, String exerciseId, ExerciseInformationInputDTO exerciseInformationInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        ExerciseCompletionInformation exerciseCompletionInformation = exerciseMapper.exerciseInformationInputDTOToExerciseInformation(exerciseInformationInputDTO);
        exerciseCompletionInformation.setExercise(exercise);
        exerciseInformationRepository.save(exerciseCompletionInformation);
    }


    public List<ExerciseInformationOutputDTO> getExerciseInformation(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("No exercise found with ID: " + exerciseId);
        }
        if (!exercise.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Patient does not have access to this exercise");
        }
        List<ExerciseCompletionInformation> exerciseCompletionInformations = exerciseInformationRepository.getExerciseInformationByExerciseId(exercise.getId());
        return exerciseMapper.exerciseInformationsToExerciseInformationOutputDTOs(exerciseCompletionInformations);
    }

}

