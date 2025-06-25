package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Exercise.*;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.ExerciseRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.repository.StoredExerciseFileRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseMediaOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.ExerciseMapper;
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
    private PatientService patientService;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @Mock
    private StoredExerciseFileRepository storedExerciseFileRepository;

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

        when(exerciseMapper.createExerciseDTOToExercise(inputDTO)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

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

        when(exerciseMapper.createExerciseDTOToExercise(inputDTO)).thenReturn(exercise);
        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        // Act
        exerciseService.createExercise(patientId, inputDTO);

        // Assert
        assertEquals(patient, exercise.getPatient());
        verify(exerciseRepository).save(exercise);
    }



}
