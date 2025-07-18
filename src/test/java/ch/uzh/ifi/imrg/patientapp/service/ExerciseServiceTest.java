package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseUpdateInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
import ch.uzh.ifi.imrg.patientapp.service.aiService.PromptBuilderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private ExerciseMapper exerciseMapper;

    @Mock
    private ExerciseInformationRepository exerciseInformationRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;

    @Mock
    private ExerciseConversationRepository exerciseConversationRepository;

    @Mock
    private AuthorizationService authorizationService;

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
        when(promptBuilderService.getSystemPrompt(any(ChatbotTemplate.class), nullable(String.class)))
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
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.getAllExercisesForCoach(patientId)
        );

        assertEquals("No patient found with ID: " + patientId, exception.getMessage());
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
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.updateExercise(patientId, exerciseId, inputDTO)
        );

        assertEquals("No exercise found with ID: " + exerciseId, ex.getMessage());
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
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.deleteExercise(patientId, exerciseId)
        );

        assertEquals("No exercise found with ID: " + exerciseId, ex.getMessage());
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
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.deleteExercise(patientId, exerciseId)
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.putExerciseFeedback(patient, exerciseId, inputDTO)
        );
        assertEquals("No exercise found with ID: " + exerciseId, ex.getMessage());

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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseInformation(patientId, exerciseId)
        );
        assertEquals("No exercise found with ID: " + exerciseId, ex.getMessage());

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
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.getExerciseChatbot(exerciseId,patient)
        );
        assertEquals("No exercise found with ID: " + exerciseId, ex.getMessage());

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
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.getExerciseChatbot(exerciseId,patient)
        );
        assertEquals("No conversation found for exercise with ID: " + exerciseId, ex.getMessage());

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



}
