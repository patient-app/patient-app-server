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

        PsychologicalTestNameOutputDTO dto1 = new PsychologicalTestNameOutputDTO("Test1");

        PsychologicalTestNameOutputDTO dto2 = new PsychologicalTestNameOutputDTO("Test2");

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

        PsychologicalTest test1 = new PsychologicalTest();
        test1.setName("DepressionTest");
        test1.setDescription("Description1");
        Patient patient = new Patient();
        patient.setId(patientId);
        test1.setPatient(patient);
        test1.setPsychologicalTestsQuestions(List.of());

        PsychologicalTest test2 = new PsychologicalTest();
        test2.setName("DepressionTest");
        test2.setDescription("Description2");
        test2.setPatient(patient);
        test2.setPsychologicalTestsQuestions(List.of());

        List<PsychologicalTest> returnedEntities = List.of(test1, test2);

        when(repository.findAllByPatientIdAndName(patientId, testName)).thenReturn(returnedEntities);

        List<PsychologicalTestOutputDTO> result = service.getPsychologicalTestResults(patientId, testName);

        assertEquals(2, result.size());
        assertEquals("Description1", result.get(0).getDescription());
        assertEquals("Description2", result.get(1).getDescription());

        verify(repository).findAllByPatientIdAndName(patientId, testName);
        verifyNoMoreInteractions(repository);
    }


}

