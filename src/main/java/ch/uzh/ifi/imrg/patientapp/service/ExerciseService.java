package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseComponentMapper;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseComponentRepository exerciseComponentRepository;
    private final PatientRepository patientRepository;
    private final ExerciseInformationRepository exerciseInformationRepository;
    private final ExerciseConversationRepository exerciseConversationRepository;
    private final PromptBuilderService promptBuilderService;
    private final AuthorizationService authorizationService;
    private final ChatbotTemplateRepository chatbotTemplateRepository;
    private final ExerciseMapper exerciseMapper;
    private final LogService logService;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseComponentRepository exerciseComponentRepository, PatientRepository patientRepository, ExerciseInformationRepository exerciseInformationRepository, ExerciseConversationRepository exerciseConversationRepository, PromptBuilderService promptBuilderService, AuthorizationService authorizationService, ChatbotTemplateRepository chatbotTemplateRepository, ExerciseMapper exerciseMapper, LogService logService) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseComponentRepository = exerciseComponentRepository;
        this.patientRepository = patientRepository;
        this.exerciseInformationRepository = exerciseInformationRepository;
        this.exerciseConversationRepository = exerciseConversationRepository;
        this.promptBuilderService = promptBuilderService;
        this.authorizationService = authorizationService;
        this.chatbotTemplateRepository = chatbotTemplateRepository;
        this.exerciseMapper = exerciseMapper;
        this.logService = logService;
    }

    public List<ExercisesOverviewOutputDTO>getAllExercisesForCoach(String patientId){
        Patient patient = patientRepository.getPatientById(patientId);
        if (patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
    }

    public List<ExerciseComponentOverviewOutputDTO> getAllExercisesComponentsOfAnExerciseForCoach(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");
        return ExerciseComponentMapper.INSTANCE.exerciseComponentsToExerciseComponentsOverviewOutputDTOs(exercise.getExerciseComponents());
    }

    public List<ExercisesOverviewOutputDTO> getExercisesOverview(Patient patient){
        List<Exercise> exercises = exerciseRepository.getExercisesByPatientId(patient.getId());
        if (exercises.isEmpty()) {
            return Collections.emptyList();
        }
        authorizationService.checkExerciseAccess(exercises.getFirst(), patient, "Patient does not have access to this exercise");
        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises);
    }

    public List<ExecutionOverviewOutputDTO> getOneExercisesOverview(Patient patient, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        List<ExerciseCompletionInformation> exerciseCompletionInformations = exerciseInformationRepository.getExerciseInformationByExerciseId(exercise.getId());
        return exerciseMapper.exerciseCompletionInformationsToExecutionOverviewOutputDTOs(exerciseCompletionInformations);
    }

    public ExerciseOutputDTO getExerciseExecution(String exerciseId, Patient patient, String exerciseExecutionId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");

        ExerciseOutputDTO exerciseOutputDTO = exerciseMapper.exerciseToExerciseOutputDTO(exercise);
        ExerciseCompletionInformation exerciseCompletionInformation = exerciseInformationRepository.getExerciseCompletionInformationById(exerciseExecutionId);
        exerciseInformationRepository.save(exerciseCompletionInformation);
        if (exerciseCompletionInformation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise execution found, create one first.");
        }
        exerciseOutputDTO.getExerciseComponents().forEach(component -> {
            ExerciseComponentAnswer answer = exerciseCompletionInformation.getComponentAnswerById(component.getId())
                    .orElse(null);
            if (answer != null) {
                component.setUserInput(answer.getUserInput());
            }
        });
        return exerciseOutputDTO;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No chatbot template found, create chatbot first. "));

        ExerciseConversation exerciseConversation = new ExerciseConversation();
        exerciseConversation.setPatient(patient);
        exerciseConversation.setSystemPrompt(promptBuilderService.getSystemPrompt(chatbotTemplate,exerciseInputDTO.getExerciseExplanation(), patient));
        exerciseConversation.setConversationName(exerciseInputDTO.getExerciseTitle()+ " - chatbot");
        exerciseConversationRepository.save(exerciseConversation);

        exercise.setExerciseConversation(exerciseConversation);
        exerciseRepository.save(exercise);

    }

    public ExerciseStartOutputDTO startExercise(String exerciseId, Patient patient) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");

        ExerciseCompletionInformation exerciseCompletionInformation = new ExerciseCompletionInformation();
        exerciseCompletionInformation.setExecutionTitle(Instant.now());
        exerciseCompletionInformation.setStartTime(Instant.now());
        exerciseCompletionInformation.setExercise(exercise);
        exerciseInformationRepository.save(exerciseCompletionInformation);

        ExerciseStartOutputDTO outputDTO = new ExerciseStartOutputDTO();
        outputDTO.setExerciseExecutionId(exerciseCompletionInformation.getId());

        logService.createLog(patient.getId(), LogTypes.EXERCISE_START,exerciseId,"" );
        return outputDTO;
    }

    public void createExerciseComponent(String patientId, String exerciseId, ExerciseComponentInputDTO exerciseComponentInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask coach to create one.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        ExerciseConversation conversation = exercise.getExerciseConversation();
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No conversation found, create one first.");
        }
        return exerciseMapper.exerciseConversationToExerciseChatbotOutputDTO(conversation);
    }

    public void updateExercise(String patientId, String exerciseId, ExerciseUpdateInputDTO exerciseUpdateInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(
                exercise,
                patientRepository.getPatientById(patientId),
                "Patient does not have access to this exercise"
        );

        // Update Exercise fields except exerciseComponents
        exerciseMapper.updateExerciseFromInputDTO(exerciseUpdateInputDTO, exercise);

        // Merge exerciseComponents
        if (exerciseUpdateInputDTO.getExerciseComponents() != null) {
            Map<String, ExerciseComponent> existingComponentsById = exercise.getExerciseComponents().stream()
                    .collect(Collectors.toMap(ExerciseComponent::getId, c -> c));

            for (ExerciseComponentInputDTO componentDTO : exerciseUpdateInputDTO.getExerciseComponents()) {
                ExerciseComponent existing = existingComponentsById.get(componentDTO.getId());
                if (existing != null) {
                    // Update existing
                    ExerciseComponentMapper.INSTANCE.updateExerciseComponentFromExerciseComponentInputDTO(componentDTO, existing);
                    existing.setExercise(exercise);
                }
            }
        }

        exerciseRepository.save(exercise);
    }



    public void updateExerciseComponent(String patientId, String exerciseId, String exerciseComponentId, ExerciseComponentUpdateInputDTO exerciseComponentUpdateInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");

        ExerciseComponent exerciseComponentFromRepository = exerciseComponentRepository.getExerciseComponentById(exerciseComponentId);
        if (exerciseComponentFromRepository == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }

        ExerciseComponentMapper.INSTANCE.updateExerciseComponentFromExerciseComponentUpdateInputDTO(exerciseComponentUpdateInputDTO, exerciseComponentFromRepository);
        exerciseComponentRepository.save(exerciseComponentFromRepository);
    }

    public void deleteExercise(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, create one first.");
        }
        if (!exercise.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not have access to this exercise");
        }
        exerciseRepository.delete(exercise);
    }

    public void deleteExerciseComponent(String patientId, String exerciseId, String exerciseComponentId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask your coach to create one.");
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");

        ExerciseComponent exerciseComponent = exerciseComponentRepository.getExerciseComponentById(exerciseComponentId);
        if (exerciseComponent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise component found, create one first.");

        }
        exercise.getExerciseComponents().remove(exerciseComponent);
        exerciseComponentRepository.delete(exerciseComponent);
    }

    public void putExerciseFeedback(Patient patient, String exerciseId, ExerciseInformationInputDTO exerciseInformationInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask coach to create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        ExerciseCompletionInformation exerciseCompletionInformation = exerciseInformationRepository.getExerciseCompletionInformationById(exerciseInformationInputDTO.getExerciseExecutionId());

        exerciseMapper.updateExerciseCompletionInformationFromExerciseCompletionInformation(exerciseInformationInputDTO, exerciseCompletionInformation);
        exerciseCompletionInformation.setExercise(exercise);

        exerciseCompletionInformation.setExerciseMoodBefore(toContainer(exerciseInformationInputDTO.getMoodsBefore()));
        exerciseCompletionInformation.setExerciseMoodAfter(toContainer(exerciseInformationInputDTO.getMoodsAfter()));

        exerciseInformationRepository.save(exerciseCompletionInformation);
        logService.createLog(patient.getId(), LogTypes.EXERCISE_COMPLETION,exerciseId, "");

    }

    public void setExerciseComponentResult(Patient patient, String exerciseId, ExerciseComponentResultInputDTO exerciseComponentResultInputDTO, String exerciseComponentId) throws Exception {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask your coach to create one.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");

        Optional <ExerciseCompletionInformation> optionalExerciseCompletionInformation =
                exerciseInformationRepository.findById(exerciseComponentResultInputDTO.getExerciseExecutionId());
        if (!optionalExerciseCompletionInformation.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise completion found, create one first.");

        }
        ExerciseCompletionInformation exerciseCompletionInformation = optionalExerciseCompletionInformation.get();
        Optional<ExerciseComponentAnswer> optional = exerciseCompletionInformation.getComponentAnswerById(exerciseComponentId);

        if (optional.isPresent()) {
            ExerciseComponentAnswer answer = optional.get();
            answer.setUserInput(exerciseComponentResultInputDTO.getUserInput());
        } else {
            ExerciseComponentAnswer answer = new ExerciseComponentAnswer();
            answer.setUserInput(exerciseComponentResultInputDTO.getUserInput());
            answer.setExerciseComponentId(exerciseComponentId);
            answer.setCompletionInformation(exerciseCompletionInformation);
            exerciseCompletionInformation.getComponentAnswers().add(answer);
        }
        exerciseInformationRepository.save(exerciseCompletionInformation);
        logService.createLog(patient.getId(), LogTypes.EXERCISE_UPDATE, exerciseId,"");

    }

    public void setExerciseCompletionName(Patient patient, String exerciseId, ExerciseCompletionNameInputDTO exerciseCompletionNameInputDTO) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask your coach to create one.");
        }
        authorizationService.checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");

        ExerciseCompletionInformation exerciseCompletionInformation = exerciseInformationRepository.getExerciseCompletionInformationById(exerciseCompletionNameInputDTO.getExerciseExecutionId());
        if (exerciseCompletionInformation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise completion found");

        }
        exerciseCompletionInformation.setExecutionTitle(exerciseCompletionNameInputDTO.getExecutionTitle());
        exerciseInformationRepository.save(exerciseCompletionInformation);
    }


    public List<ExerciseInformationOutputDTO> getExerciseInformation(String patientId, String exerciseId) {
        Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No exercise found, ask your coach to create one first.");
        }
        authorizationService.checkExerciseAccess(exercise, patientRepository.getPatientById(patientId), "Patient does not have access to this exercise");
        List<ExerciseCompletionInformation> exerciseCompletionInformations = exerciseInformationRepository.getExerciseInformationByExerciseId(exercise.getId());
        return exerciseMapper.exerciseInformationsToExerciseInformationOutputDTOs(exerciseCompletionInformations);
    }

    private ExerciseMoodContainer toContainer(List<ExerciseMoodInputDTO> input) {
        if (input == null || input.isEmpty()) return null;

        ExerciseMoodContainer container = new ExerciseMoodContainer();
        List<ExerciseMood> moods = exerciseMapper.mapMoodInputDTOsToExerciseMoods(input);

        // Set back-reference
        for (ExerciseMood mood : moods) {
            mood.setExerciseMoodContainer(container);
        }

        container.setExerciseMoods(moods);
        return container;
    }


    public List<ExercisesOverviewOutputDTO> getExercisesForDashboard(Patient patient){
        Instant now = Instant.now();

        List<Exercise> candidates = exerciseRepository.findAllActiveExercisesWithinTime(now);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        authorizationService.checkExerciseAccess(candidates.getFirst(), patient, "Patient does not have access to this exercise");

        List<Exercise> filtered = candidates.stream()
                .filter(e -> {
                    List<ExerciseCompletionInformation> completions = e.getExerciseCompletionInformation();

                    if (completions == null || completions.isEmpty()) {
                        return true;
                    }

                    ExerciseCompletionInformation latest = completions.stream()
                            .filter(c -> c.getEndTime() != null)
                            .max(Comparator.comparing(ExerciseCompletionInformation::getEndTime))
                            .orElse(null);

                    if (latest == null) return true;

                    Instant dueDate = latest.getEndTime().plus(e.getDoEveryNDays(), ChronoUnit.DAYS);
                    Instant threshold = dueDate.minus(24, ChronoUnit.HOURS);

                    return now.isAfter(threshold);
                })
                .toList();

        return exerciseMapper.exercisesToExerciseOverviewOutputDTOs(filtered);
    }


}

