package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.ChatbotTemplate;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.*;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseMediaOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
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
    private StoredExerciseFileRepository storedExerciseFileRepository;

    @Mock
    private ExerciseInformationRepository exerciseInformationRepository;

    @Mock
    private ChatbotTemplateRepository chatbotTemplateRepository;

    @Mock
    private ExerciseConversationRepository exerciseConversationRepository;


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
    void testGetExercise_WhenExerciseExists_ReturnsMappedDTO() {
        String id = "ex123";
        Exercise exercise = new Exercise();
        ExerciseOutputDTO dto = new ExerciseOutputDTO();

        when(exerciseRepository.getExerciseById(id)).thenReturn(exercise);
        when(exerciseMapper.exerciseToExerciseOutputDTO(exercise)).thenReturn(dto);

        ExerciseOutputDTO result = exerciseService.getExercise(id);

        assertEquals(dto, result);
        verify(exerciseRepository).getExerciseById(id);
        verify(exerciseMapper).exerciseToExerciseOutputDTO(exercise);
    }

    @Test
    void testGetExercise_WhenExerciseNotExists_ThrowsException() {
        String id = "nonexistent";

        when(exerciseRepository.getExerciseById(id)).thenReturn(null);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.getExercise(id)
        );

        assertEquals("No exercise found with ID: " + id, thrown.getMessage());
        verify(exerciseRepository).getExerciseById(id);
        verifyNoInteractions(exerciseMapper);
    }
    @Test
    void testGetExerciseMedia_ValidRequest_ReturnsDTO() {
        // Arrange
        String exerciseId = "ex123";
        String mediaId = "media123";

        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise = new Exercise();
        exercise.setPatient(patient); // same patient

        StoredExerciseFile mediaFile = new StoredExerciseFile();
        ExerciseMediaOutputDTO expectedDTO = new ExerciseMediaOutputDTO();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(storedExerciseFileRepository.findById(mediaId)).thenReturn(Optional.of(mediaFile));
        when(exerciseMapper.storedExerciseFileToExerciseMediaOutputDTO(mediaFile)).thenReturn(expectedDTO);

        // Act
        ExerciseMediaOutputDTO result = exerciseService.getExerciseMedia(patient, exerciseId, mediaId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(storedExerciseFileRepository).findById(mediaId);
        verify(exerciseMapper).storedExerciseFileToExerciseMediaOutputDTO(mediaFile);
    }

    @Test
    void testGetExerciseMedia_ExerciseNotFound_ThrowsException() {
        String exerciseId = "ex404";
        String mediaId = "media123";
        Patient patient = new Patient();
        patient.setId("p1");

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseMedia(patient, exerciseId, mediaId)
        );

        assertEquals("No exercise found with ID: " + exerciseId, exception.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoInteractions(storedExerciseFileRepository);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void testGetExerciseMedia_WrongPatient_ThrowsException() {
        String exerciseId = "ex123";
        String mediaId = "media123";

        Patient caller = new Patient();
        caller.setId("caller");

        Patient owner = new Patient();
        owner.setId("owner");

        Exercise exercise = new Exercise();
        exercise.setPatient(owner); // different patient

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseMedia(caller, exerciseId, mediaId)
        );

        assertEquals("Patient does not have access to this exercise", exception.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoInteractions(storedExerciseFileRepository);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void testGetExerciseMedia_MediaNotFound_ThrowsException() {
        String exerciseId = "ex123";
        String mediaId = "media404";

        Patient patient = new Patient();
        patient.setId("p1");

        Exercise exercise = new Exercise();
        exercise.setPatient(patient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(storedExerciseFileRepository.findById(mediaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseMedia(patient, exerciseId, mediaId)
        );

        assertEquals("No media found with ID: " + mediaId, exception.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(storedExerciseFileRepository).findById(mediaId);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void testCreateExercise_WithExerciseElements_SetsAllFieldsAndSaves() {
        // Arrange
        String patientId = "p1";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        Patient patient = new Patient();
        patient.setId(patientId);

        ExerciseElement e1 = new ExerciseFileElement();
        ExerciseElement e2 = new ExerciseImageElement();
        List<ExerciseElement> elements = List.of(e1, e2);

        Exercise exercise = new Exercise();
        exercise.setExerciseElements(elements);

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
        assertEquals(exercise, e1.getExercise());
        assertEquals(exercise, e2.getExercise());
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void testCreateExercise_WithNullExerciseElements_SavesWithoutError() {
        // Arrange
        String patientId = "p2";
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        Patient patient = new Patient();
        patient.setId(patientId);

        Exercise exercise = new Exercise();
        exercise.setExerciseElements(null); // key difference here

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
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

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
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

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
        ExerciseInputDTO inputDTO = new ExerciseInputDTO();

        Patient differentPatient = new Patient();
        differentPatient.setId("otherPatient");

        Exercise existingExercise = new Exercise();
        existingExercise.setPatient(differentPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(existingExercise);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.updateExercise(patientId, exerciseId, inputDTO)
        );

        assertEquals("Patient does not have access to this exercise", ex.getMessage());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
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
    void testPutExerciseFeedback_SuccessfullySavesExerciseInformation() {
        // Arrange
        String exerciseId = "ex1";
        Patient patient = new Patient();
        patient.setId("p123");

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(patient);

        ExerciseInformationInputDTO inputDTO = new ExerciseInformationInputDTO();
        ExerciseInformation mappedInfo = new ExerciseInformation();

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);
        when(exerciseMapper.exerciseInformationInputDTOToExerciseInformation(inputDTO)).thenReturn(mappedInfo);

        // Act
        exerciseService.putExerciseFeedback(patient, exerciseId, inputDTO);

        // Assert
        assertEquals(exercise, mappedInfo.getExercise());
        verify(exerciseRepository).getExerciseById(exerciseId);
        verify(exerciseMapper).exerciseInformationInputDTOToExerciseInformation(inputDTO);
        verify(exerciseInformationRepository).save(mappedInfo);
        verifyNoMoreInteractions(exerciseRepository, exerciseMapper, exerciseInformationRepository);
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

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.putExerciseFeedback(actualPatient, exerciseId, inputDTO)
        );
        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
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

        List<ExerciseInformation> exerciseInfoList = List.of(new ExerciseInformation(), new ExerciseInformation());
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

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setPatient(actualPatient);

        when(exerciseRepository.getExerciseById(exerciseId)).thenReturn(exercise);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exerciseService.getExerciseInformation(patientId, exerciseId)
        );
        assertEquals("Patient does not have access to this exercise", ex.getMessage());

        verify(exerciseRepository).getExerciseById(exerciseId);
        verifyNoMoreInteractions(exerciseRepository, exerciseInformationRepository, exerciseMapper);
    }


}
