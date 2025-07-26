package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import org.jsoup.HttpStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ch.qos.logback.core.util.AggregationType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ExerciseComponentRepository exerciseComponentRepository;
    @Mock
    private ExerciseMapper exerciseMapper;

    @Mock
    private ExerciseInformationRepository exerciseInformationRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;

    @Mock
    private ExerciseConversationRepository exerciseConversationRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private LogService logService;

    @InjectMocks
    private ExerciseService exerciseService;


    @Test
    void testGetExercisesOverview_ReturnsMappedDTOs() {
        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise1 = new Exercise();
        Exercise exercise2 = new Exercise();
        List<Exercise> exercises = List.of(exercise1, exercise2);

        ExercisesOverviewOutputDTO dto1 = new ExercisesOverviewOutputDTO();
        ExercisesOverviewOutputDTO dto2 = new ExercisesOverviewOutputDTO();
        List<ExercisesOverviewOutputDTO> expected = List.of(dto1, dto2);

        when(exerciseRepository.getExercisesByPatientId("p123")).thenReturn(exercises);
        when(exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exercises)).thenReturn(expected);

        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesOverview(patient);

        assertEquals(expected, result);
        verify(exerciseRepository).getExercisesByPatientId("p123");
        verify(exerciseMapper).exercisesToExerciseOverviewOutputDTOs(exercises);
    }

    @Test
    void getExercisesOverview_returnsEmptyList_whenNoExercisesFound() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        when(exerciseRepository.getExercisesByPatientId(patient.getId())).thenReturn(Collections.emptyList());

        // Act
        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesOverview(patient);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that no authorization or mapping happened
        verify(authorizationService, never()).checkExerciseAccess(any(), any(), anyString());
        verify(exerciseMapper, never()).exercisesToExerciseOverviewOutputDTOs(any());
    }



    @Test
    void testCreateExercise_WithNullExerciseElements_SavesWithoutError() {
        // Arrange
        String patientId = "p2";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setExerciseComponents(null); // key difference here

        when(exerciseMapper.exerciseInputDTOToExercise(inputDTO)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId))
                .thenReturn(List.of(new ChatbotTemplate()));
        when(promptBuilderService.getSystemPrompt(any(ChatbotTemplate.class), nullable(String.class), any(Patient.class)))
                .thenReturn("dummy system prompt");
        when(exerciseConversationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        exerciseService.createExercise(patientId, inputDTO);

        // Assert
        assertEquals(patient, exercise.getPatient());
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void getAllExercisesForCoach_returnsMappedExercises_whenPatientExists() {
        // Arrange
        String patientId = "patient123";
        Patient patient = new Patient();
        patient.setId(patientId);

        List<Exercise> exerciseList = List.of(new Exercise());
        List<ExercisesOverviewOutputDTO> dtoList = List.of(new ExercisesOverviewOutputDTO());

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(exerciseRepository.getExercisesByPatientId(patientId)).thenReturn(exerciseList);
        when(exerciseMapper.exercisesToExerciseOverviewOutputDTOs(exerciseList)).thenReturn(dtoList);

        // Act
        List<ExercisesOverviewOutputDTO> result = exerciseService.getAllExercisesForCoach(patientId);

        // Assert
        assertEquals(dtoList, result);
        verify(patientRepository).getPatientById(patientId);
        verify(exerciseRepository).getExercisesByPatientId(patientId);
        verify(exerciseMapper).exercisesToExerciseOverviewOutputDTOs(exerciseList);
        verifyNoMoreInteractions(patientRepository, exerciseRepository, exerciseMapper);
    }

    @Test
    void getAllExercisesForCoach_throwsException_whenPatientNotFound() {
        // Arrange
        String patientId = "nonexistent";
        when(patientRepository.getPatientById(patientId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.getAllExercisesForCoach(patientId)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", exception.getMessage());
        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository, exerciseRepository, exerciseMapper);
    }

    @Test
    void updateExercise_updatesExerciseSuccessfully_whenValid() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";
        ExerciseUpdateInputDTO inputDTO = new ExerciseUpdateInputDTO();

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise existingExercise = new Exercise();
        existingExercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);

        // Act
        exerciseService.updateExercise(patientId, exerciseId, inputDTO);

        // Assert
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseMapper).updateExerciseFromInputDTO(inputDTO, existingExercise);
        verify(exerciseRepository).save(existingExercise);
        verifyNoMoreInteractions(exerciseRepository, exerciseMapper);
    }

    @Test
    void updateExercise_throwsException_whenExerciseNotFound() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";
        ExerciseUpdateInputDTO inputDTO = new ExerciseUpdateInputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(patientId, exerciseId, inputDTO)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void updateExercise_throwsException_whenPatientMismatch() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";
        ExerciseUpdateInputDTO inputDTO = new ExerciseUpdateInputDTO();

        Patient differentPatient = new Patient();
        differentPatient.setId("otherPatient");

        Exercise existingExercise = new Exercise();
        existingExercise.setId(exerciseId);
        existingExercise.setPatient(differentPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(differentPatient); // you can also return a different one

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService)
                .checkExerciseAccess(eq(existingExercise), eq(differentPatient), anyString());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.updateExercise(patientId, exerciseId, inputDTO)
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(existingExercise), eq(differentPatient), anyString());
        verifyNoMoreInteractions(exerciseRepository, exerciseMapper);
    }


    @Test
    void deleteExercise_deletesExercise_whenValid() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        // Act
        exerciseService.deleteExercise(patientId, exerciseId);

        // Assert
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseRepository).delete(exercise);
        verifyNoMoreInteractions(exerciseRepository);
    }

    @Test
    void deleteExercise_throwsException_whenExerciseNotFound() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.deleteExercise(patientId, exerciseId)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
    }

    @Test
    void deleteExercise_throwsException_whenPatientMismatch() {
        // Arrange
        String patientId = "patient123";
        String exerciseId = "exercise456";

        Patient otherPatient = new Patient();
        otherPatient.setId("differentPatient");

        Exercise exercise = new Exercise();
        exercise.setPatient(otherPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        // Act + Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.deleteExercise(patientId, exerciseId)
        );

        assertEquals("403 FORBIDDEN \"You do not have access to this exercise\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
    }

    @Test
    void testPutExerciseFeedback_ThrowsWhenExerciseNotFound() {
        // Arrange
        String exerciseId = "ex2";
        Patient patient = new Patient();
        patient.setId("p123");
        ExerciseInformationInputDTO inputDTO = new ExerciseInformationInputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.putExerciseFeedback(patient, exerciseId, inputDTO)
        );
        assertEquals("404 NOT_FOUND \"No exercise found, ask coach to create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, exerciseMapper, exerciseInformationRepository);
    }

    @Test
    void testPutExerciseFeedback_ThrowsWhenPatientMismatch() {
        // Arrange
        String exerciseId = "ex3";

        Patient actualPatient = new Patient();
        actualPatient.setId("actual");

        Patient otherPatient = new Patient();
        otherPatient.setId("other");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(otherPatient);

        ExerciseInformationInputDTO inputDTO = new ExerciseInformationInputDTO();
        inputDTO.setExerciseExecutionId("dummy-id");

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(eq(exercise), eq(actualPatient), anyString());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.putExerciseFeedback(actualPatient, exerciseId, inputDTO)
        );
        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(actualPatient), anyString());
        verifyNoMoreInteractions(exerciseRepository, exerciseMapper, exerciseInformationRepository);
    }


    @Test
    void testGetExerciseInformation_ReturnsDTOList() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e123";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        List<ExerciseCompletionInformation> exerciseInfoList = List.of(new ExerciseCompletionInformation(), new ExerciseCompletionInformation());
        List<ExerciseInformationOutputDTO> expectedDTOs = List.of(new ExerciseInformationOutputDTO(), new ExerciseInformationOutputDTO());

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(exerciseInformationRepository.getExerciseInformationByExerciseId(exerciseId)).thenReturn(exerciseInfoList);
        when(exerciseMapper.exerciseInformationsToExerciseInformationOutputDTOs(exerciseInfoList)).thenReturn(expectedDTOs);

        // Act
        List<ExerciseInformationOutputDTO> result = exerciseService.getExerciseInformation(patientId, exerciseId);

        // Assert
        assertEquals(expectedDTOs, result);
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseInformationRepository).getExerciseInformationByExerciseId(exerciseId);
        verify(exerciseMapper).exerciseInformationsToExerciseInformationOutputDTOs(exerciseInfoList);
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository, exerciseMapper);
    }

    @Test
    void testGetExerciseInformation_ThrowsWhenExerciseNotFound() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e999";

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.getExerciseInformation(patientId, exerciseId)
        );
        assertEquals("404 NOT_FOUND \"No exercise found, ask your coach to create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository, exerciseMapper);
    }

    @Test
    void testGetExerciseInformation_ThrowsWhenPatientMismatch() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e456";

        Patient actualPatient = new Patient();
        actualPatient.setId("wrong-id");

        Patient requestedPatient = new Patient();
        requestedPatient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(actualPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(requestedPatient);

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(eq(exercise), eq(requestedPatient), anyString());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseInformation(patientId, exerciseId)
        );
        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(requestedPatient), anyString());
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository, exerciseMapper);
    }


    @Test
    void testGetExerciseChatbot_ThrowsWhenExerciseNotFound() {
        Patient patient = new Patient();
        // Arrange
        String exerciseId = "e404";
        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.getExerciseChatbot(exerciseId,patient)
        );
        assertEquals("404 NOT_FOUND \"No exercise found, ask coach to create one.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void testGetExerciseChatbot_ThrowsWhenConversationNotFound() {
        Patient patient = new Patient();
        // Arrange
        String exerciseId = "e123";
        Exercise exercise = new Exercise();
        exercise.setExerciseConversation(null);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        // Act + Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.getExerciseChatbot(exerciseId,patient)
        );
        assertEquals("404 NOT_FOUND \"No conversation found, create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void testGetExerciseChatbot_ReturnsDTO_WhenConversationExists() {
        // Arrange
        String exerciseId = "e123";
        ExerciseConversation conversation = new ExerciseConversation();
        Exercise exercise = new Exercise();
        exercise.setExerciseConversation(conversation);

        ExerciseChatbotOutputDTO expectedDTO = new ExerciseChatbotOutputDTO();
        Patient patient = new Patient();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(exerciseMapper.exerciseConversationToExerciseChatbotOutputDTO(conversation)).thenReturn(expectedDTO);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());        // Act
        ExerciseChatbotOutputDTO result = exerciseService.getExerciseChatbot(exerciseId,patient);

        // Assert
        assertEquals(expectedDTO, result);
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseMapper).exerciseConversationToExerciseChatbotOutputDTO(conversation);
    }

    @Test
    void getAllExercisesComponentsOfAnExerciseForCoach_throwsException_whenExerciseNotFound() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e999";

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.getAllExercisesComponentsOfAnExerciseForCoach(patientId, exerciseId)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, patientRepository, authorizationService);
    }

    @Test
    void getAllExercisesComponentsOfAnExerciseForCoach_throwsException_whenPatientMismatch() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e456";

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(new Patient());

        Patient requestingPatient = new Patient();
        requestingPatient.setId(patientId);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(requestingPatient);
        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(eq(exercise), eq(requestingPatient), anyString());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getAllExercisesComponentsOfAnExerciseForCoach(patientId, exerciseId)
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(requestingPatient), anyString());
        verifyNoMoreInteractions(exerciseRepository, patientRepository, authorizationService);
    }


    @Test
    void getAllExercisesComponentsOfAnExerciseForCoach_returnsDTOList_whenAuthorized() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e789";

        Patient patient = new Patient();
        patient.setId(patientId);

        ExerciseComponent component1 = new ExerciseComponent();
        component1.setId("c1");

        ExerciseComponent component2 = new ExerciseComponent();
        component2.setId("c2");

        List<ExerciseComponent> components = List.of(component1, component2);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);
        exercise.setExerciseComponents(components);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());

        // Act
        List<ExerciseComponentOverviewOutputDTO> result = exerciseService.getAllExercisesComponentsOfAnExerciseForCoach(patientId, exerciseId);

        // Assert
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
    }
    @Test
    void getOneExercisesOverview_returnsDTOs_whenAuthorized() {
        // Arrange
        String exerciseId = "e123";
        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        // Provide meaningful values
        ExerciseCompletionInformation info1 = new ExerciseCompletionInformation();
        info1.setId("info1");
        info1.setExercise(exercise);

        ExerciseCompletionInformation info2 = new ExerciseCompletionInformation();
        info2.setId("info2");
        info2.setExercise(exercise);

        List<ExerciseCompletionInformation> infos = List.of(info1, info2);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(exerciseInformationRepository.getExerciseInformationByExerciseId(exerciseId)).thenReturn(infos);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());

        // Act
        List<ExecutionOverviewOutputDTO> result = exerciseService.getOneExercisesOverview(patient, exerciseId);

        // Assert
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseInformationRepository).getExerciseInformationByExerciseId(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
    }



    @Test
    void getOneExercisesOverview_throwsException_whenExerciseNotFound() {
        // Arrange
        String exerciseId = "not_found";
        Patient patient = new Patient();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.getOneExercisesOverview(patient, exerciseId)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository);
    }

    @Test
    void getOneExercisesOverview_throwsException_whenAccessDenied() {
        // Arrange
        String exerciseId = "e123";
        Patient patient = new Patient();
        patient.setId("wrongPatient");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(new Patient()); // different patient

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getOneExercisesOverview(patient, exerciseId)
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verifyNoMoreInteractions(exerciseInformationRepository);
    }

    @Test
    void getExerciseExecution_returnsDTO_whenValid() {
        // Arrange
        String exerciseId = "e123";
        String executionId = "exec456";
        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseCompletionInformation completionInfo = new ExerciseCompletionInformation();
        completionInfo.setId(executionId);

        ExerciseOutputDTO expectedDTO = new ExerciseOutputDTO();
        expectedDTO.setExerciseComponents(new ArrayList<>());

        ExerciseComponentOutputDTO component = new ExerciseComponentOutputDTO();
        component.setId("comp1");
        expectedDTO.getExerciseComponents().add(component);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseMapper.exerciseToExerciseOutputDTO(exercise)).thenReturn(expectedDTO);
        when(exerciseInformationRepository.getExerciseCompletionInformationById(executionId)).thenReturn(completionInfo);

        // Act
        ExerciseOutputDTO result = exerciseService.getExerciseExecution(exerciseId, patient, executionId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        verify(exerciseMapper).exerciseToExerciseOutputDTO(exercise);
        verify(exerciseInformationRepository).getExerciseCompletionInformationById(executionId);
        verify(exerciseInformationRepository).save(completionInfo);
    }

    @Test
    void getExerciseExecution_throwsException_whenExerciseNotFound() {
        // Arrange
        String exerciseId = "e404";
        Patient patient = new Patient();
        String executionId = "exec123";

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.getExerciseExecution(exerciseId, patient, executionId));

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
        verifyNoInteractions(authorizationService, exerciseMapper, exerciseInformationRepository);
    }

    @Test
    void getExerciseExecution_throwsException_whenUnauthorized() {
        // Arrange
        String exerciseId = "e123";
        String executionId = "exec456";
        Patient patient = new Patient();
        patient.setId("unauthorized");

        Patient otherPatient = new Patient();
        otherPatient.setId("owner");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(otherPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> exerciseService.getExerciseExecution(exerciseId, patient, executionId));

        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verifyNoInteractions(exerciseMapper, exerciseInformationRepository);
    }

    @Test
    void getExerciseExecution_throwsException_whenCompletionInformationIsNull() {
        // Arrange
        String exerciseId = "e123";
        String executionId = "exec456";
        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseOutputDTO mappedDTO = new ExerciseOutputDTO();
        mappedDTO.setExerciseComponents(new ArrayList<>());

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseMapper.exerciseToExerciseOutputDTO(exercise)).thenReturn(mappedDTO);
        when(exerciseInformationRepository.getExerciseCompletionInformationById(executionId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.getExerciseExecution(exerciseId, patient, executionId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No exercise execution found"));
    }

    @Test
    void getExerciseExecution_setsUserInput_whenAnswerExists() {
        // Arrange
        String exerciseId = "e123";
        String executionId = "exec456";
        String componentId = "comp1";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseComponentOutputDTO componentDTO = new ExerciseComponentOutputDTO();
        componentDTO.setId(componentId);
        List<ExerciseComponentOutputDTO> componentList = new ArrayList<>();
        componentList.add(componentDTO);

        ExerciseOutputDTO mappedDTO = new ExerciseOutputDTO();
        mappedDTO.setExerciseComponents(componentList);

        ExerciseComponentAnswer answer = new ExerciseComponentAnswer();
        answer.setExerciseComponentId(componentId);
        answer.setUserInput("Test input");

        ExerciseCompletionInformation completionInfo = Mockito.spy(new ExerciseCompletionInformation());
        completionInfo.setId(executionId);
        List<ExerciseComponentAnswer> answerList = new ArrayList<>();
        answerList.add(answer);
        completionInfo.setComponentAnswers(answerList);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseMapper.exerciseToExerciseOutputDTO(exercise)).thenReturn(mappedDTO);
        when(exerciseInformationRepository.getExerciseCompletionInformationById(executionId)).thenReturn(completionInfo);

        // Act
        ExerciseOutputDTO result = exerciseService.getExerciseExecution(exerciseId, patient, executionId);

        // Assert
        assertEquals("Test input", result.getExerciseComponents().get(0).getUserInput());
    }



    @Test
    void createExercise_setsExerciseInComponents_whenComponentsPresent() {
        // Arrange
        String patientId = "p123";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();
        inputDTO.setExerciseTitle("My Exercise");
        inputDTO.setExerciseExplanation("Explanation");

        Patient patient = new Patient();
        patient.setId(patientId);

        // Components with null .getExercise() initially
        ExerciseComponent component1 = new ExerciseComponent();
        ExerciseComponent component2 = new ExerciseComponent();
        List<ExerciseComponent> components = List.of(component1, component2);

        Exercise exercise = new Exercise();
        exercise.setExerciseComponents(components);

        ChatbotTemplate chatbotTemplate = new ChatbotTemplate();

        when(exerciseMapper.exerciseInputDTOToExercise(inputDTO)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(chatbotTemplateRepository.findByPatientId(patientId)).thenReturn(List.of(chatbotTemplate));
        when(promptBuilderService.getSystemPrompt(chatbotTemplate, inputDTO.getExerciseExplanation(),patient)).thenReturn("prompt");
        when(exerciseConversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        exerciseService.createExercise(patientId, inputDTO);

        // Assert: each component's back-reference must be set
        assertEquals(exercise, component1.getExercise());
        assertEquals(exercise, component2.getExercise());

        verify(exerciseRepository).save(exercise);
        verify(exerciseConversationRepository).save(any());
    }

    @Test
    void startExercise_createsAndReturnsExerciseExecution_whenAuthorized() {
        // Arrange
        String exerciseId = "e123";
        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseCompletionInformation savedInfo = new ExerciseCompletionInformation();
        savedInfo.setId("execution789");
        savedInfo.setExercise(exercise);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.save(any(ExerciseCompletionInformation.class))).thenAnswer(invocation -> {
            ExerciseCompletionInformation info = invocation.getArgument(0);
            info.setId("execution789"); // simulate JPA ID assignment
            return info;
        });

        // Act
        ExerciseStartOutputDTO result = exerciseService.startExercise(exerciseId, patient);

        // Assert
        assertEquals("execution789", result.getExerciseExecutionId());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseInformationRepository).save(any(ExerciseCompletionInformation.class));
    }

    @Test
    void startExercise_throwsException_whenExerciseNotFound() {
        // Arrange
        String exerciseId = "not-found";
        Patient patient = new Patient();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.startExercise(exerciseId, patient)
        );
        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository, authorizationService);
    }

    @Test
    void createExerciseComponent_addsComponent_whenAuthorized_withoutMapper() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e456";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);
        exercise.setExerciseComponents(new ArrayList<>());

        // Create a real input DTO with expected values
        ExerciseComponentInputDTO inputDTO = new ExerciseComponentInputDTO();
        inputDTO.setExerciseComponentDescription("Description");
        inputDTO.setFileName("FileName");

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        doNothing().when(authorizationService)
                .checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");

        // Act
        exerciseService.createExerciseComponent(patientId, exerciseId, inputDTO);

        // Assert
        assertEquals(1, exercise.getExerciseComponents().size());
        ExerciseComponent created = exercise.getExerciseComponents().get(0);
        assertEquals("Description", created.getExerciseComponentDescription());
        assertEquals("FileName", created.getFileName());
        assertSame(exercise, created.getExercise());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        verify(exerciseRepository).save(exercise);
    }


    @Test
    void createExerciseComponent_throwsException_whenExerciseNotFound() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "not-found";
        ExerciseComponentInputDTO inputDTO = new ExerciseComponentInputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.createExerciseComponent(patientId, exerciseId, inputDTO)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, patientRepository, authorizationService);
    }

    @Test
    void createExerciseComponent_throwsException_whenUnauthorized() {
        // Arrange
        String patientId = "p123";
        String exerciseId = "e123";

        Patient actualPatient = new Patient();
        actualPatient.setId("other");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(actualPatient);
        exercise.setExerciseComponents(new ArrayList<>());

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(actualPatient);

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService).checkExerciseAccess(exercise, actualPatient, "Patient does not have access to this exercise");

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.createExerciseComponent(patientId, exerciseId, new ExerciseComponentInputDTO())
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(exercise, actualPatient, "Patient does not have access to this exercise");
        verifyNoMoreInteractions(exerciseRepository);
    }

    @Test
    void updateExercise_mergesExerciseComponents_whenIdsMatch() {
        // Arrange
        String patientId = "patient1";
        String exerciseId = "exercise1";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise existingExercise = new Exercise();
        existingExercise.setId(exerciseId);
        existingExercise.setPatient(patient);

        ExerciseComponent component = new ExerciseComponent();
        component.setId("comp1");
        component.setFileName("Old Text");
        existingExercise.setExerciseComponents(new ArrayList<>(List.of(component)));

        ExerciseComponentInputDTO componentDTO = new ExerciseComponentInputDTO();
        componentDTO.setId("comp1");
        componentDTO.setFileName("Updated Text");

        ExerciseUpdateInputDTO updateInputDTO = new ExerciseUpdateInputDTO();
        updateInputDTO.setExerciseComponents(List.of(componentDTO));

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);

        // Act
        exerciseService.updateExercise(patientId, exerciseId, updateInputDTO);

        // Assert
        assertEquals("Updated Text", existingExercise.getExerciseComponents().get(0).getFileName());
        assertEquals(existingExercise, existingExercise.getExerciseComponents().get(0).getExercise());
        verify(exerciseRepository).save(existingExercise);
    }

    @Test
    void updateExercise_skipsComponentUpdate_whenIdsDoNotMatch() {
        // Arrange
        String patientId = "patient1";
        String exerciseId = "exercise1";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise existingExercise = new Exercise();
        existingExercise.setId(exerciseId);
        existingExercise.setPatient(patient);

        ExerciseComponent existingComponent = new ExerciseComponent();
        existingComponent.setId("existing");
        existingComponent.setFileName("Old Text");
        existingExercise.setExerciseComponents(new ArrayList<>(List.of(existingComponent)));

        ExerciseComponentInputDTO newComponentDTO = new ExerciseComponentInputDTO();
        newComponentDTO.setId("nonMatchingId");
        newComponentDTO.setFileName("New Text");

        ExerciseUpdateInputDTO updateInputDTO = new ExerciseUpdateInputDTO();
        updateInputDTO.setExerciseComponents(List.of(newComponentDTO));

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);

        // Act
        exerciseService.updateExercise(patientId, exerciseId, updateInputDTO);

        // Assert
        assertEquals("Old Text", existingExercise.getExerciseComponents().get(0).getFileName());
        verify(exerciseRepository).save(existingExercise);
    }

    @Test
    void updateExercise_doesNotTouchComponents_whenUpdateInputHasNullComponents() {
        // Arrange
        String patientId = "patient1";
        String exerciseId = "exercise1";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise existingExercise = new Exercise();
        existingExercise.setId(exerciseId);
        existingExercise.setPatient(patient);

        ExerciseComponent existingComponent = new ExerciseComponent();
        existingComponent.setId("comp1");
        existingComponent.setFileName("Should Remain");
        existingExercise.setExerciseComponents(new ArrayList<>(List.of(existingComponent)));

        ExerciseUpdateInputDTO updateInputDTO = new ExerciseUpdateInputDTO();
        updateInputDTO.setExerciseComponents(null); // This skips the merge block

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);

        // Act
        exerciseService.updateExercise(patientId, exerciseId, updateInputDTO);

        // Assert
        assertEquals("Should Remain", existingExercise.getExerciseComponents().get(0).getFileName());
        verify(exerciseRepository).save(existingExercise);
    }

    @Test
    void updateExerciseComponent_updatesSuccessfully_whenEverythingIsValid() {
        String patientId = "p123";
        String exerciseId = "e456";
        String componentId = "c789";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseComponent component = new ExerciseComponent();
        component.setId(componentId);
        component.setFileName("Old Text");

        ExerciseComponentUpdateInputDTO updateDTO = new ExerciseComponentUpdateInputDTO();
        updateDTO.setFileName("Updated Text");

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(exerciseComponentRepository.getExerciseComponentById(componentId)).thenReturn(component);

        exerciseService.updateExerciseComponent(patientId, exerciseId, componentId, updateDTO);

        assertEquals("Updated Text", component.getFileName());
        verify(exerciseComponentRepository).save(component);
    }


    @Test
    void updateExerciseComponent_throwsException_whenExerciseNotFound() {
        String exerciseId = "missing-exercise";
        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExerciseComponent("p1", exerciseId, "c1", new ExerciseComponentUpdateInputDTO())
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());
        verifyNoInteractions(patientRepository, exerciseComponentRepository);
    }

    @Test
    void updateExerciseComponent_throwsException_whenUnauthorized() {
        String patientId = "p123";
        String exerciseId = "e456";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        doThrow(new IllegalArgumentException("Patient does not have access to this exercise"))
                .when(authorizationService)
                .checkExerciseAccess(eq(exercise), eq(patient), anyString());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.updateExerciseComponent(patientId, exerciseId, "c1", new ExerciseComponentUpdateInputDTO())
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());
        verifyNoInteractions(exerciseComponentRepository);
    }


    @Test
    void updateExerciseComponent_throwsException_whenExerciseComponentNotFound() {
        // Arrange
        String patientId = "p1";
        String exerciseId = "e1";
        String componentId = "c1";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseComponentRepository.getExerciseComponentById(componentId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.updateExerciseComponent(patientId, exerciseId, componentId, new ExerciseComponentUpdateInputDTO())
        );

        assertEquals("404 NOT_FOUND \"No exercise found, create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseComponentRepository).getExerciseComponentById(componentId);
        verifyNoMoreInteractions(exerciseRepository, patientRepository, authorizationService, exerciseComponentRepository);
    }
    @Test
    void deleteExerciseComponent_deletesSuccessfully_whenValid() {
        // Arrange
        String patientId = "p1";
        String exerciseId = "e1";
        String componentId = "c1";

        Patient patient = new Patient();
        patient.setId(patientId);

        ExerciseComponent component = new ExerciseComponent();
        component.setId(componentId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);
        exercise.setExerciseComponents(new ArrayList<>(List.of(component)));

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(exerciseComponentRepository.getExerciseComponentById(componentId)).thenReturn(component);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());

        // Act
        exerciseService.deleteExerciseComponent(patientId, exerciseId, componentId);

        // Assert
        assertFalse(exercise.getExerciseComponents().contains(component));
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseComponentRepository).getExerciseComponentById(componentId);
        verify(exerciseComponentRepository).delete(component);
        verifyNoMoreInteractions(exerciseRepository, patientRepository, authorizationService, exerciseComponentRepository);
    }

    @Test
    void deleteExerciseComponent_throwsException_whenExerciseNotFound() {
        // Arrange
        String exerciseId = "eX";
        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.deleteExerciseComponent("p1", exerciseId, "c1")
        );

        assertEquals("404 NOT_FOUND \"No exercise found, ask your coach to create one.\"", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
    }

    @Test
    void deleteExerciseComponent_throwsException_whenComponentNotFound() {
        // Arrange
        String patientId = "p1";
        String exerciseId = "e1";
        String componentId = "missing";

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseComponentRepository.getExerciseComponentById(componentId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.deleteExerciseComponent(patientId, exerciseId, componentId)
        );

        assertEquals("404 NOT_FOUND \"No exercise component found, create one first.\"", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(patientRepository).getPatientById(patientId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseComponentRepository).getExerciseComponentById(componentId);
        verifyNoMoreInteractions(exerciseRepository, patientRepository, exerciseComponentRepository, authorizationService);
    }

    @Test
    void putExerciseFeedback_updatesExerciseCompletionInformationWithMoodEntities() {
        // Arrange
        String exerciseId = "e123";
        String executionId = "exec123";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseCompletionInformation existingInfo = new ExerciseCompletionInformation();
        existingInfo.setId(executionId);

        ExerciseMoodInputDTO moodBefore1 = new ExerciseMoodInputDTO();
        moodBefore1.setMoodName("anxious");
        moodBefore1.setMoodScore(3);

        ExerciseMoodInputDTO moodAfter1 = new ExerciseMoodInputDTO();
        moodAfter1.setMoodName("relieved");
        moodAfter1.setMoodScore(8);

        List<ExerciseMoodInputDTO> moodsBefore = List.of(moodBefore1);
        List<ExerciseMoodInputDTO> moodsAfter = List.of(moodAfter1);

        // These are what the mapper is expected to return
        ExerciseMood mappedMoodBefore = new ExerciseMood();
        mappedMoodBefore.setMoodName("anxious");
        mappedMoodBefore.setMoodScore(3);

        ExerciseMood mappedMoodAfter = new ExerciseMood();
        mappedMoodAfter.setMoodName("relieved");
        mappedMoodAfter.setMoodScore(8);

        // Mocking the container generation via mapper call inside toContainer()
        when(exerciseMapper.mapMoodInputDTOsToExerciseMoods(moodsBefore)).thenReturn(List.of(mappedMoodBefore));
        when(exerciseMapper.mapMoodInputDTOsToExerciseMoods(moodsAfter)).thenReturn(List.of(mappedMoodAfter));

        ExerciseInformationInputDTO inputDTO = new ExerciseInformationInputDTO();
        inputDTO.setExerciseExecutionId(executionId);
        inputDTO.setMoodsBefore(moodsBefore);
        inputDTO.setMoodsAfter(moodsAfter);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(exerciseInformationRepository.getExerciseCompletionInformationById(executionId)).thenReturn(existingInfo);

        // Act
        exerciseService.putExerciseFeedback(patient, exerciseId, inputDTO);

        // Assert
        assertEquals(exercise, existingInfo.getExercise());

        List<ExerciseMood> storedBefore = existingInfo.getExerciseMoodBefore().getExerciseMoods();
        List<ExerciseMood> storedAfter = existingInfo.getExerciseMoodAfter().getExerciseMoods();

        assertEquals(1, storedBefore.size());
        assertEquals("anxious", storedBefore.get(0).getMoodName());
        assertEquals(3, storedBefore.get(0).getMoodScore());

        assertEquals(1, storedAfter.size());
        assertEquals("relieved", storedAfter.get(0).getMoodName());
        assertEquals(8, storedAfter.get(0).getMoodScore());

        // Confirm back-references
        assertEquals(existingInfo.getExerciseMoodBefore(), storedBefore.get(0).getExerciseMoodContainer());
        assertEquals(existingInfo.getExerciseMoodAfter(), storedAfter.get(0).getExerciseMoodContainer());

        // Verifications
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(exercise, patient, "Patient does not have access to this exercise");
        verify(exerciseInformationRepository).getExerciseCompletionInformationById(executionId);
        verify(exerciseMapper).updateExerciseCompletionInformationFromExerciseCompletionInformation(inputDTO, existingInfo);
        verify(exerciseMapper).mapMoodInputDTOsToExerciseMoods(moodsBefore);
        verify(exerciseMapper).mapMoodInputDTOsToExerciseMoods(moodsAfter);
        verify(exerciseInformationRepository).save(existingInfo);
    }

    @Test
    void setExerciseComponentResult_updatesExistingAnswer() throws Exception {
        // Arrange
        String exerciseId = "ex123";
        String execId = "exec123";
        String componentId = "comp1";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseComponentResultInputDTO dto = new ExerciseComponentResultInputDTO();
        dto.setExerciseExecutionId(execId);
        dto.setUserInput("Updated Answer");

        ExerciseComponentAnswer existingAnswer = new ExerciseComponentAnswer();
        existingAnswer.setExerciseComponentId(componentId);

        ExerciseCompletionInformation completionInfo = new ExerciseCompletionInformation();
        completionInfo.setId(execId);
        completionInfo.getComponentAnswers().add(existingAnswer);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.findById(execId)).thenReturn(Optional.of(completionInfo));
        doNothing().when(logService).createLog(anyString(), any(LogTypes.class), anyString(), eq(""));        // Act
        exerciseService.setExerciseComponentResult(patient, exerciseId, dto, componentId);

        // Assert
        assertEquals("Updated Answer", existingAnswer.getUserInput());
        verify(exerciseInformationRepository).save(completionInfo);
    }

    @Test
    void setExerciseComponentResult_createsNewAnswerIfNotFound() throws Exception {
        // Arrange
        String exerciseId = "ex123";
        String execId = "exec456";
        String componentId = "newComp";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseComponentResultInputDTO dto = new ExerciseComponentResultInputDTO();
        dto.setExerciseExecutionId(execId);
        dto.setUserInput("New Answer");

        ExerciseCompletionInformation completionInfo = new ExerciseCompletionInformation();
        completionInfo.setId(execId); // No answers yet

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.findById(execId)).thenReturn(Optional.of(completionInfo));
        doNothing().when(logService).createLog(anyString(), any(LogTypes.class), anyString(), eq(""));        // Act

        exerciseService.setExerciseComponentResult(patient, exerciseId, dto, componentId);

        // Assert
        assertEquals(1, completionInfo.getComponentAnswers().size());
        ExerciseComponentAnswer savedAnswer = completionInfo.getComponentAnswers().get(0);
        assertEquals("New Answer", savedAnswer.getUserInput());
        assertEquals(componentId, savedAnswer.getExerciseComponentId());
        assertEquals(completionInfo, savedAnswer.getCompletionInformation());
        verify(exerciseInformationRepository).save(completionInfo);
    }

    @Test
    void setExerciseComponentResult_throwsIfExerciseNotFound() {
        // Arrange
        String exerciseId = "nonexistent";
        ExerciseComponentResultInputDTO dto = new ExerciseComponentResultInputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.setExerciseComponentResult(new Patient(), exerciseId, dto, "comp1"));

        assertEquals("404 NOT_FOUND \"No exercise found, ask your coach to create one.\"", ex.getMessage());
    }

    @Test
    void setExerciseComponentResult_throwsIfCompletionInfoNotFound() {
        // Arrange
        String exerciseId = "ex123";
        String execId = "missingExec";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseComponentResultInputDTO dto = new ExerciseComponentResultInputDTO();
        dto.setExerciseExecutionId(execId);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.findById(execId)).thenReturn(Optional.empty());

        // Act + Assert
        Exception ex = assertThrows(Exception.class, () ->
                exerciseService.setExerciseComponentResult(patient, exerciseId, dto, "comp1"));

        assertEquals("404 NOT_FOUND \"No exercise completion found, create one first.\"", ex.getMessage());
    }

    @Test
    void setExerciseCompletionName_updatesTitle_whenValid() {
        // Arrange
        String exerciseId = "ex123";
        String execId = "exec456";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseCompletionInformation completionInfo = new ExerciseCompletionInformation();
        completionInfo.setId(execId);
        Instant title = Instant.now();

        ExerciseCompletionNameInputDTO inputDTO = new ExerciseCompletionNameInputDTO();
        inputDTO.setExerciseExecutionId(execId);
        inputDTO.setExecutionTitle(title);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.getExerciseCompletionInformationById(execId)).thenReturn(completionInfo);

        // Act
        exerciseService.setExerciseCompletionName(patient, exerciseId, inputDTO);

        // Assert
        assertEquals(title, completionInfo.getExecutionTitle());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseInformationRepository).getExerciseCompletionInformationById(execId);
        verify(exerciseInformationRepository).save(completionInfo);
    }

    @Test
    void setExerciseCompletionName_throwsWhenExerciseNotFound() {
        // Arrange
        String exerciseId = "missing";
        ExerciseCompletionNameInputDTO inputDTO = new ExerciseCompletionNameInputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.setExerciseCompletionName(new Patient(), exerciseId, inputDTO)
        );

        assertEquals("404 NOT_FOUND \"No exercise found, ask your coach to create one.\"", ex.getMessage());
    }

    @Test
    void setExerciseCompletionName_throwsWhenCompletionInfoMissing() {
        // Arrange
        String exerciseId = "ex123";
        String execId = "missingExec";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseCompletionNameInputDTO inputDTO = new ExerciseCompletionNameInputDTO();
        inputDTO.setExerciseExecutionId(execId);
        inputDTO.setExecutionTitle(Instant.now());

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        doNothing().when(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        when(exerciseInformationRepository.getExerciseCompletionInformationById(execId)).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exerciseService.setExerciseCompletionName(patient, exerciseId, inputDTO)
        );

        assertEquals("404 NOT_FOUND \"No exercise completion found\"", ex.getMessage());
    }

    @Test
    void getExercisesForDashboard_shouldFilterAndMapCorrectly() {
        // Given
        Patient patient = new Patient();
        patient.setId("p1");

        ExerciseCompletionInformation info = new ExerciseCompletionInformation();
        Instant latestCompletion = Instant.now().minus(10, ChronoUnit.DAYS);
        info.setEndTime(latestCompletion);

        Exercise exercise = new Exercise();
        exercise.setPatient(patient);
        exercise.setDoEveryNDays(7);
        exercise.setExerciseCompletionInformation(List.of(info));

        List<Exercise> activeExercises = List.of(exercise);
        when(exerciseRepository.findAllActiveExercisesWithinTime(any())).thenReturn(activeExercises);

        ExercisesOverviewOutputDTO dto = new ExercisesOverviewOutputDTO();
        when(exerciseMapper.exercisesToExerciseOverviewOutputDTOs(anyList())).thenReturn(List.of(dto));

        // When
        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesForDashboard(patient);

        // Then
        assertEquals(1, result.size());
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseMapper).exercisesToExerciseOverviewOutputDTOs(anyList());
    }

    @Test
    void getExercisesForDashboard_shouldReturnEmpty_whenNoCandidates() {
        when(exerciseRepository.findAllActiveExercisesWithinTime(any())).thenReturn(Collections.emptyList());

        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesForDashboard(new Patient());

        assertTrue(result.isEmpty());
        verifyNoInteractions(authorizationService);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void getExercisesForDashboard_shouldIncludeExercise_whenCompletionsAreNullOrEmpty() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exerciseWithNullCompletions = new Exercise();
        exerciseWithNullCompletions.setPatient(patient);
        exerciseWithNullCompletions.setDoEveryNDays(7);
        exerciseWithNullCompletions.setExerciseCompletionInformation(null);

        Exercise exerciseWithEmptyCompletions = new Exercise();
        exerciseWithEmptyCompletions.setPatient(patient);
        exerciseWithEmptyCompletions.setDoEveryNDays(7);
        exerciseWithEmptyCompletions.setExerciseCompletionInformation(Collections.emptyList());

        List<Exercise> activeExercises = List.of(exerciseWithNullCompletions, exerciseWithEmptyCompletions);
        when(exerciseRepository.findAllActiveExercisesWithinTime(any())).thenReturn(activeExercises);

        // Stub authorization and mapping
        doNothing().when(authorizationService).checkExerciseAccess(any(), any(), anyString());

        List<ExercisesOverviewOutputDTO> mappedDTOs = List.of(new ExercisesOverviewOutputDTO(), new ExercisesOverviewOutputDTO());
        when(exerciseMapper.exercisesToExerciseOverviewOutputDTOs(anyList())).thenReturn(mappedDTOs);

        // Act
        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesForDashboard(patient);

        // Assert
        assertEquals(2, result.size()); // Both exercises should be included
        verify(authorizationService).checkExerciseAccess(eq(exerciseWithNullCompletions), eq(patient), anyString());
        verify(exerciseMapper).exercisesToExerciseOverviewOutputDTOs(anyList());
    }

    @Test
    void getExercisesForDashboard_shouldIncludeExercise_whenLatestCompletionHasNullEndTime() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setPatient(patient);
        exercise.setDoEveryNDays(7);

        ExerciseCompletionInformation incomplete = new ExerciseCompletionInformation();
        incomplete.setEndTime(null); // <-- Important: endTime is null

        exercise.setExerciseCompletionInformation(List.of(incomplete));

        when(exerciseRepository.findAllActiveExercisesWithinTime(any())).thenReturn(List.of(exercise));

        doNothing().when(authorizationService).checkExerciseAccess(any(), any(), anyString());

        List<ExercisesOverviewOutputDTO> mappedDTOs = List.of(new ExercisesOverviewOutputDTO());
        when(exerciseMapper.exercisesToExerciseOverviewOutputDTOs(anyList())).thenReturn(mappedDTOs);

        // Act
        List<ExercisesOverviewOutputDTO> result = exerciseService.getExercisesForDashboard(patient);

        // Assert
        assertEquals(1, result.size()); // Should include this exercise
        verify(authorizationService).checkExerciseAccess(eq(exercise), eq(patient), anyString());
        verify(exerciseMapper).exercisesToExerciseOverviewOutputDTOs(anyList());
    }


}
