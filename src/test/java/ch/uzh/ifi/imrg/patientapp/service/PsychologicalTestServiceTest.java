package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestsAssignmentRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestAssignmentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestQuestionInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameAndPatientIdOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsychologicalTestServiceTest {

    @Mock
    private PsychologicalTestRepository repository;

    @Mock
    private PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PsychologicalTestService service;

    @Test
    void createPsychologicalTest_shouldSetPatientAndBackReferences_andSave() throws Exception {
        // Arrange
        String patientId = "123";
        Patient patient = new Patient();
        patient.setId(patientId);

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName("Test Name");

        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();
        inputDTO.setName("Test Name");
        inputDTO.setDescription("Test Description");

        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName("123", "Test Name"))
                .thenReturn(assignment);

        // Act
        service.createPsychologicalTest(patient, inputDTO);

        // Assert
        ArgumentCaptor<PsychologicalTest> captor = ArgumentCaptor.forClass(PsychologicalTest.class);
        verify(repository).save(captor.capture());

        PsychologicalTest saved = captor.getValue();
        assertSame(patient, saved.getPatient());

        if (saved.getPsychologicalTestsQuestions() != null) {
            for (PsychologicalTestQuestions q : saved.getPsychologicalTestsQuestions()) {
                assertSame(saved, q.getPsychologicalTest());
            }
        }

        verify(psychologicalTestsAssignmentRepository).save(assignment);
        assertNotNull(assignment.getLastCompletedAt());
        verifyNoMoreInteractions(repository);
    }


    @Test
    void createPsychologicalTest_whenQuestionsPresent_setsBackReferencesAndSaves() throws Exception {
        Patient patient = new Patient();
        patient.setId("123");

        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();
        inputDTO.setName("Test Name");
        inputDTO.setDescription("Test Description");

        PsychologicalTestQuestionInputDTO q1 = new PsychologicalTestQuestionInputDTO();
        q1.setQuestion("Question 1");
        q1.setScore(5);

        PsychologicalTestQuestionInputDTO q2 = new PsychologicalTestQuestionInputDTO();
        q2.setQuestion("Question 2");
        q2.setScore(3);

        inputDTO.setQuestions(List.of(q1, q2));

        // ✅ Mock the assignment repository to return an assignment
        PsychologicalTestAssignment mockAssignment = new PsychologicalTestAssignment();
        mockAssignment.setTestName("Test Name");
        mockAssignment.setPatient(patient);
        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(any(), any()))
                .thenReturn(mockAssignment);

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
    void getAllTestNamesForPatient_ForCoach_shouldReturnListFromRepository() {
        String patientId = "p123";

        PsychologicalTestNameOutputDTO dto1 = new PsychologicalTestNameOutputDTO("Test1");

        PsychologicalTestNameOutputDTO dto2 = new PsychologicalTestNameOutputDTO("Test2");

        List<PsychologicalTestNameOutputDTO> expected = List.of(dto1, dto2);

        when(repository.findAllByPatientId(patientId)).thenReturn(expected);

        List<PsychologicalTestNameOutputDTO> result = service.getAllTestNamesForPatientForCoach(patientId);

        assertEquals(expected, result);
        verify(repository).findAllByPatientId(patientId);
        verifyNoMoreInteractions(repository);
    }
    @Test
    void getPsychologicalTestResults_ForCoach_shouldReturnListFromRepository() {
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

        List<PsychologicalTestOutputDTO> result = service.getPsychologicalTestResultsForCoach(patientId, testName);

        assertEquals(2, result.size());
        assertEquals("Description1", result.get(0).getDescription());
        assertEquals("Description2", result.get(1).getDescription());

        verify(repository).findAllByPatientIdAndName(patientId, testName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllTestNamesForPatient_whenListIsEmpty_shouldReturnEmptyList() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        when(repository.findAllTestsByPatientId("p1")).thenReturn(List.of());

        List<PsychologicalTestNameAndPatientIdOutputDTO> result =
                service.getAllTestNamesForPatient(loggedInPatient, "p1");

        assertTrue(result.isEmpty());
        verify(repository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllTestNamesForPatient_whenPatientMatches_shouldReturnList() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        PsychologicalTestNameAndPatientIdOutputDTO dto =
                new PsychologicalTestNameAndPatientIdOutputDTO("Test A", "p1");
        dto.setPatientId("p1");

        List<PsychologicalTestNameAndPatientIdOutputDTO> expected = List.of(dto);

        when(repository.findAllTestsByPatientId("p1")).thenReturn(expected);

        List<PsychologicalTestNameAndPatientIdOutputDTO> result =
                service.getAllTestNamesForPatient(loggedInPatient, "p1");

        assertEquals(expected, result);
        verify(repository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllTestNamesForPatient_whenPatientMismatch_shouldThrowAccessDenied() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        PsychologicalTestNameAndPatientIdOutputDTO dto =
                new PsychologicalTestNameAndPatientIdOutputDTO("Test A", "p2");
        dto.setPatientId("differentPatient");

        when(repository.findAllTestsByPatientId("p1")).thenReturn(List.of(dto));

        assertThrows(AccessDeniedException.class, () ->
                service.getAllTestNamesForPatient(loggedInPatient, "p1"));

        verify(repository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPsychologicalTestResults_whenEmptyList_shouldReturnEmpty() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        when(repository.findAllByPatientIdAndName("p1", "DepressionTest")).thenReturn(List.of());

        List<PsychologicalTestOutputDTO> result =
                service.getPsychologicalTestResults(loggedInPatient, "p1", "DepressionTest");

        assertTrue(result.isEmpty());
        verify(repository).findAllByPatientIdAndName("p1", "DepressionTest");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPsychologicalTestResults_whenPatientMatches_shouldReturnList() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        Patient testOwner = new Patient();
        testOwner.setId("p1");

        PsychologicalTest test = new PsychologicalTest();
        test.setName("TestName");
        test.setDescription("TestDesc");
        test.setPatient(testOwner);
        test.setPsychologicalTestsQuestions(List.of());

        when(repository.findAllByPatientIdAndName("p1", "TestName")).thenReturn(List.of(test));

        List<PsychologicalTestOutputDTO> result =
                service.getPsychologicalTestResults(loggedInPatient, "p1", "TestName");

        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getPatientId());
        assertEquals("TestName", result.get(0).getName());

        verify(repository).findAllByPatientIdAndName("p1", "TestName");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPsychologicalTestResults_whenPatientMismatch_shouldThrow() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("loggedIn");

        Patient testOwner = new Patient();
        testOwner.setId("someoneElse");

        PsychologicalTest test = new PsychologicalTest();
        test.setName("TestName");
        test.setPatient(testOwner);
        test.setPsychologicalTestsQuestions(List.of());

        when(repository.findAllByPatientIdAndName("p1", "TestName")).thenReturn(List.of(test));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                service.getPsychologicalTestResults(loggedInPatient, "p1", "TestName"));

        assertEquals("You do not have access to this patient's psychological tests.", ex.getMessage());

        verify(repository).findAllByPatientIdAndName("p1", "TestName");
        verifyNoMoreInteractions(repository);
    }


}

