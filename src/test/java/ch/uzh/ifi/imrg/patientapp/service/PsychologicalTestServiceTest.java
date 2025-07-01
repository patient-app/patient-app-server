package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestQuestionInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsychologicalTestServiceTest {

    @Mock
    private PsychologicalTestRepository repository;

    @InjectMocks
    private PsychologicalTestService service;

    @Test
    void createPsychologicalTest_shouldSetPatientAndBackReferences_andSave() {
        // Arrange
        Patient patient = new Patient();
        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();


        inputDTO.setName("Test Name");
        inputDTO.setDescription("Test Description");

        // Act
        service.createPsychologicalTest(patient, inputDTO);

        // Assert
        ArgumentCaptor<PsychologicalTest> captor = ArgumentCaptor.forClass(PsychologicalTest.class);
        verify(repository).save(captor.capture());

        PsychologicalTest saved = captor.getValue();

        assertSame(patient, saved.getPatient());

        // Check that back-references are set if questions exist
        if (saved.getPsychologicalTestsQuestions() != null) {
            for (PsychologicalTestQuestions q : saved.getPsychologicalTestsQuestions()) {
                assertSame(saved, q.getPsychologicalTest());
            }
        }

        verifyNoMoreInteractions(repository);
    }

    @Test
    void createPsychologicalTest_whenQuestionsPresent_setsBackReferencesAndSaves() {
        Patient patient = new Patient();

        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();
        inputDTO.setName("Test Name");
        inputDTO.setDescription("Test Description");

        // You must have a list of questions in your DTO if your mapper expects them:
        PsychologicalTestQuestionInputDTO q1 = new PsychologicalTestQuestionInputDTO();
        q1.setQuestion("Question 1");
        q1.setScore(5);

        PsychologicalTestQuestionInputDTO q2 = new PsychologicalTestQuestionInputDTO();
        q2.setQuestion("Question 2");
        q2.setScore(3);

        inputDTO.setQuestions(List.of(q1, q2));

        service.createPsychologicalTest(patient, inputDTO);

        ArgumentCaptor<PsychologicalTest> captor = ArgumentCaptor.forClass(PsychologicalTest.class);
        verify(repository).save(captor.capture());
        PsychologicalTest saved = captor.getValue();

        assertSame(patient, saved.getPatient());
        assertNotNull(saved.getPsychologicalTestsQuestions());
        assertEquals(2, saved.getPsychologicalTestsQuestions().size());

        for (PsychologicalTestQuestions q : saved.getPsychologicalTestsQuestions()) {
            assertSame(saved, q.getPsychologicalTest());
        }

        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllTestNamesForPatient_shouldReturnListFromRepository() {
        String patientId = "p123";

        PsychologicalTestNameOutputDTO dto1 = new PsychologicalTestNameOutputDTO();
        dto1.setName("Test1");
        PsychologicalTestNameOutputDTO dto2 = new PsychologicalTestNameOutputDTO();
        dto2.setName("Test2");

        List<PsychologicalTestNameOutputDTO> expected = List.of(dto1, dto2);

        when(repository.findAllByPatientId(patientId)).thenReturn(expected);

        List<PsychologicalTestNameOutputDTO> result = service.getAllTestNamesForPatient(patientId);

        assertEquals(expected, result);
        verify(repository).findAllByPatientId(patientId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPsychologicalTestResults_shouldReturnListFromRepository() {
        String patientId = "p123";
        String testName = "DepressionTest";

        PsychologicalTestOutputDTO dto1 = new PsychologicalTestOutputDTO("DepressionTest");
        dto1.setDescription("Description1");
        dto1.setPatientId(patientId);
        dto1.setQuestions(List.of());

        PsychologicalTestOutputDTO dto2 = new PsychologicalTestOutputDTO("DepressionTest");
        dto2.setDescription("Description2");
        dto2.setPatientId(patientId);
        dto2.setQuestions(List.of());

        List<PsychologicalTestOutputDTO> expected = List.of(dto1, dto2);

        when(repository.findAllByPatientIdAndName(patientId, testName)).thenReturn(expected);

        List<PsychologicalTestOutputDTO> result = service.getPsychologicalTestResults(patientId, testName);

        assertEquals(expected, result);
        verify(repository).findAllByPatientIdAndName(patientId, testName);
        verifyNoMoreInteractions(repository);
    }

}

