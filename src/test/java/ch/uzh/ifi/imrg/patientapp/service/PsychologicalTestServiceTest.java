package ch.uzh.ifi.imrg.patientapp.service;

import ch.uzh.ifi.imrg.patientapp.constant.AvailableTests;
import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
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
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.*;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PsychologicalTestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsychologicalTestServiceTest {

    @Mock
    private PsychologicalTestRepository psychologicalTestRepository;

    @Mock
    private PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private LogService logService;

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
        doNothing().when(logService).createLog(anyString(), any(LogTypes.class), anyString(), eq(""));
        // Act
        service.createPsychologicalTest(patient, inputDTO);

        // Assert
        ArgumentCaptor<PsychologicalTest> captor = ArgumentCaptor.forClass(PsychologicalTest.class);
        verify(psychologicalTestRepository).save(captor.capture());

        PsychologicalTest saved = captor.getValue();
        assertSame(patient, saved.getPatient());

        if (saved.getPsychologicalTestsQuestions() != null) {
            for (PsychologicalTestQuestions q : saved.getPsychologicalTestsQuestions()) {
                assertSame(saved, q.getPsychologicalTest());
            }
        }

        verify(psychologicalTestsAssignmentRepository).save(assignment);
        assertNotNull(assignment.getLastCompletedAt());
        verifyNoMoreInteractions(psychologicalTestRepository);
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
        doNothing().when(logService).createLog(anyString(), any(LogTypes.class), anyString(), eq(""));

        service.createPsychologicalTest(patient, inputDTO);

        ArgumentCaptor<PsychologicalTest> captor = ArgumentCaptor.forClass(PsychologicalTest.class);
        verify(psychologicalTestRepository).save(captor.capture());
        PsychologicalTest saved = captor.getValue();

        assertSame(patient, saved.getPatient());
        assertNotNull(saved.getPsychologicalTestsQuestions());
        assertEquals(2, saved.getPsychologicalTestsQuestions().size());

        for (PsychologicalTestQuestions q : saved.getPsychologicalTestsQuestions()) {
            assertSame(saved, q.getPsychologicalTest());
        }

        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void createPsychologicalTest_shouldCreateNewAssignment_whenAssignmentIsNull() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        PsychologicalTestInputDTO inputDTO = new PsychologicalTestInputDTO();
        inputDTO.setName("TestName");
        inputDTO.setDescription("Some desc");

        // Return null to simulate missing assignment
        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName("p1", "TestName"))
                .thenReturn(null);

        // Mock the mapper
        PsychologicalTest test = new PsychologicalTest();
        test.setName("TestName");
        test = PsychologicalTestMapper.INSTANCE.convertPsychologicalTestInputDTOToPsychologicalTest(inputDTO);

        // Act
        service.createPsychologicalTest(patient, inputDTO);

        // Assert
        ArgumentCaptor<PsychologicalTestAssignment> assignmentCaptor = ArgumentCaptor.forClass(PsychologicalTestAssignment.class);
        verify(psychologicalTestsAssignmentRepository).save(assignmentCaptor.capture());

        PsychologicalTestAssignment savedAssignment = assignmentCaptor.getValue();
        assertEquals("TestName", savedAssignment.getTestName());
        assertEquals(patient, savedAssignment.getPatient());
        assertFalse(savedAssignment.getIsPaused());
        assertEquals(1000, savedAssignment.getDoEveryNDays());

        verify(psychologicalTestRepository).save(argThat(savedTest ->
                savedTest.getName().equals("TestName")
        ));
        verify(logService).createLog(eq(patient.getId()), eq(LogTypes.PSYCHOLOGICAL_TEST_COMPLETED), eq("TestName"), eq(""));
    }




    @Test
    void getAllTestNamesForPatient_ForCoach_shouldReturnListFromRepository() {
        String patientId = "p123";

        PsychologicalTestNameOutputDTO dto1 = new PsychologicalTestNameOutputDTO("Test1");

        PsychologicalTestNameOutputDTO dto2 = new PsychologicalTestNameOutputDTO("Test2");

        List<PsychologicalTestNameOutputDTO> expected = List.of(dto1, dto2);

        when(psychologicalTestRepository.findAllByPatientId(patientId)).thenReturn(expected);

        List<PsychologicalTestNameOutputDTO> result = service.getAllTestNamesForPatientForCoach(patientId);

        assertEquals(expected, result);
        verify(psychologicalTestRepository).findAllByPatientId(patientId);
        verifyNoMoreInteractions(psychologicalTestRepository);
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

        when(psychologicalTestRepository.findAllByPatientIdAndName(patientId, testName)).thenReturn(returnedEntities);

        List<PsychologicalTestOutputDTO> result = service.getPsychologicalTestResultsForCoach(patientId, testName);

        assertEquals(2, result.size());
        assertEquals("Description1", result.get(0).getDescription());
        assertEquals("Description2", result.get(1).getDescription());

        verify(psychologicalTestRepository).findAllByPatientIdAndName(patientId, testName);
        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void getAllTestNamesForPatient_whenListIsEmpty_shouldReturnEmptyList() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        when(psychologicalTestRepository.findAllTestsByPatientId("p1")).thenReturn(List.of());

        List<PsychologicalTestNameAndPatientIdOutputDTO> result =
                service.getAllTestNamesForPatient(loggedInPatient, "p1");

        assertTrue(result.isEmpty());
        verify(psychologicalTestRepository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void getAllTestNamesForPatient_whenPatientMatches_shouldReturnList() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        PsychologicalTestNameAndPatientIdOutputDTO dto =
                new PsychologicalTestNameAndPatientIdOutputDTO("Test A", "p1");
        dto.setPatientId("p1");

        List<PsychologicalTestNameAndPatientIdOutputDTO> expected = List.of(dto);

        when(psychologicalTestRepository.findAllTestsByPatientId("p1")).thenReturn(expected);

        List<PsychologicalTestNameAndPatientIdOutputDTO> result =
                service.getAllTestNamesForPatient(loggedInPatient, "p1");

        assertEquals(expected, result);
        verify(psychologicalTestRepository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void getAllTestNamesForPatient_whenPatientMismatch_shouldThrowAccessDenied() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        PsychologicalTestNameAndPatientIdOutputDTO dto =
                new PsychologicalTestNameAndPatientIdOutputDTO("Test A", "p2");
        dto.setPatientId("differentPatient");

        when(psychologicalTestRepository.findAllTestsByPatientId("p1")).thenReturn(List.of(dto));

        assertThrows(AccessDeniedException.class, () ->
                service.getAllTestNamesForPatient(loggedInPatient, "p1"));

        verify(psychologicalTestRepository).findAllTestsByPatientId("p1");
        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void getPsychologicalTestResults_whenEmptyList_shouldReturnEmpty() {
        Patient loggedInPatient = new Patient();
        loggedInPatient.setId("p1");

        when(psychologicalTestRepository.findAllByPatientIdAndName("p1", "DepressionTest")).thenReturn(List.of());

        List<PsychologicalTestOutputDTO> result =
                service.getPsychologicalTestResults(loggedInPatient, "p1", "DepressionTest");

        assertTrue(result.isEmpty());
        verify(psychologicalTestRepository).findAllByPatientIdAndName("p1", "DepressionTest");
        verifyNoMoreInteractions(psychologicalTestRepository);
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

        when(psychologicalTestRepository.findAllByPatientIdAndName("p1", "TestName")).thenReturn(List.of(test));

        List<PsychologicalTestOutputDTO> result =
                service.getPsychologicalTestResults(loggedInPatient, "p1", "TestName");

        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getPatientId());
        assertEquals("TestName", result.get(0).getName());

        verify(psychologicalTestRepository).findAllByPatientIdAndName("p1", "TestName");
        verifyNoMoreInteractions(psychologicalTestRepository);
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

        when(psychologicalTestRepository.findAllByPatientIdAndName("p1", "TestName")).thenReturn(List.of(test));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                service.getPsychologicalTestResults(loggedInPatient, "p1", "TestName"));

        assertEquals("You do not have access to this patient's psychological tests.", ex.getMessage());

        verify(psychologicalTestRepository).findAllByPatientIdAndName("p1", "TestName");
        verifyNoMoreInteractions(psychologicalTestRepository);
    }

    @Test
    void createPsychologicalTestAssignment_shouldMapAndSaveAssignment() {
        // Arrange
        String patientId = "p1";
        String testName = "GAD7";

        Patient patient = new Patient();
        patient.setId(patientId);

        PsychologicalTestAssignmentInputDTO inputDTO = new PsychologicalTestAssignmentInputDTO();
        inputDTO.setDoEveryNDays(7);
        inputDTO.setExerciseStart(Instant.now());
        inputDTO.setExerciseEnd(Instant.now().plus(7, ChronoUnit.DAYS));
        inputDTO.setIsPaused(false);

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);

        // Act
        service.createPsychologicalTestAssginment(patientId, testName, inputDTO);

        // Assert
        ArgumentCaptor<PsychologicalTestAssignment> captor = ArgumentCaptor.forClass(PsychologicalTestAssignment.class);
        verify(psychologicalTestsAssignmentRepository).save(captor.capture());

        PsychologicalTestAssignment saved = captor.getValue();
        assertEquals(testName, saved.getTestName());
        assertSame(patient, saved.getPatient());
        assertEquals(7, saved.getDoEveryNDays());
        assertEquals(inputDTO.getExerciseStart(), saved.getExerciseStart());
        assertEquals(inputDTO.getExerciseEnd(), saved.getExerciseEnd());
        assertFalse(saved.getIsPaused());

        verify(patientRepository).getPatientById(patientId);
        verifyNoMoreInteractions(patientRepository, psychologicalTestsAssignmentRepository);
    }

    @Test
    void updatePsychologicalTestAssignment_shouldUpdateAndSave_whenAssignmentExistsAndAccessAllowed() {
        // Arrange
        String patientId = "p1";
        String testName = "GAD7";

        Patient patient = new Patient();
        patient.setId(patientId);

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName(testName);

        PsychologicalTestAssignmentInputDTO inputDTO = new PsychologicalTestAssignmentInputDTO();
        inputDTO.setDoEveryNDays(5);
        inputDTO.setIsPaused(true);

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, testName))
                .thenReturn(assignment);

        // Act
        service.updatePsychologicalTestAssginment(patientId, testName, inputDTO);

        // Assert
        verify(psychologicalTestsAssignmentRepository).save(assignment);
        assertEquals(5, assignment.getDoEveryNDays());
        assertTrue(assignment.getIsPaused());

        verify(authorizationService).checkPsychologicalTestAssignmentAccess(assignment, patient,
                "You do not have access to this patient's psychological test assignment.");
    }

    @Test
    void updatePsychologicalTestAssignment_shouldThrow_whenAssignmentNotFound() {
        // Arrange
        String patientId = "p1";
        String testName = "GAD7";

        Patient patient = new Patient();
        patient.setId(patientId);

        PsychologicalTestAssignmentInputDTO inputDTO = new PsychologicalTestAssignmentInputDTO();

        when(patientRepository.getPatientById(patientId)).thenReturn(patient);
        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, testName))
                .thenReturn(null);

        // Act + Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                service.updatePsychologicalTestAssginment(patientId, testName, inputDTO));

        assertEquals("No psychological test assignment found for the given patient and test name.", ex.getMessage());
    }

    @Test
    void updatePsychologicalTestAssignment_shouldThrow_whenAccessDenied() {
        // Arrange
        String patientId = "p1";
        String testName = "GAD7";

        Patient loggedIn = new Patient();
        loggedIn.setId(patientId);

        Patient different = new Patient();
        different.setId("someoneElse");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(different); // <-- not same as logged in
        assignment.setTestName(testName);

        PsychologicalTestAssignmentInputDTO inputDTO = new PsychologicalTestAssignmentInputDTO();

        when(patientRepository.getPatientById(patientId)).thenReturn(loggedIn);
        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, testName))
                .thenReturn(assignment);

        doThrow(new AccessDeniedException("You do not have access to this patient's psychological test assignment."))
                .when(authorizationService)
                .checkPsychologicalTestAssignmentAccess(assignment, loggedIn,
                        "You do not have access to this patient's psychological test assignment.");

        // Act + Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                service.updatePsychologicalTestAssginment(patientId, testName, inputDTO));

    }
    @Test
    void getAllAvailableTestNamesForPatientForCoach_shouldReturnAllEnumNames() {
        // Act
        List<PsychologicalTestNameOutputDTO> result = service.getAllAvailableTestNamesForPatientForCoach("anyPatientId");

        // Assert
        List<String> expectedTestNames = Arrays.stream(AvailableTests.values())
                .map(Enum::name)
                .toList();

        List<String> actualTestNames = result.stream()
                .map(PsychologicalTestNameOutputDTO::getName)
                .toList();

        assertEquals(expectedTestNames.size(), actualTestNames.size());
        assertTrue(actualTestNames.containsAll(expectedTestNames));
    }

    @Test
    void getPsychologicalTestsForDashboard_whenNoAssignments_returnsEmptyList() {
        Patient patient = new Patient();
        patient.setId("p1");

        when(psychologicalTestsAssignmentRepository.findActiveAssignments(eq(patient), any()))
                .thenReturn(List.of());

        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        assertTrue(result.isEmpty());
        verify(psychologicalTestsAssignmentRepository).findActiveAssignments(eq(patient), any());
    }

    @Test
    void returnsEmptyListWhenNoAssignments() {
        Patient patient = new Patient();
        when(psychologicalTestsAssignmentRepository.findActiveAssignments(any(), any()))
                .thenReturn(Collections.emptyList());

        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        assertTrue(result.isEmpty());
        verify(authorizationService, never()).checkPsychologicalTestAssignmentAccess(any(), any(), anyString());
    }

    @Test
    void getPsychologicalTestsForDashboard_withLastCompletedNull_shouldReturnAssignment() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName("TestX");
        assignment.setLastCompletedAt(null); // this triggers the null branch
        assignment.setDoEveryNDays(3);

        when(psychologicalTestsAssignmentRepository.findActiveAssignments(eq(patient), any()))
                .thenReturn(List.of(assignment));

        // Act
        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        // Assert
        assertEquals(1, result.size());
        verify(authorizationService).checkPsychologicalTestAssignmentAccess(eq(assignment), eq(patient),
                eq("You do not have access to this patient's psychological tests."));
    }

    @Test
    void getPsychologicalTestsForDashboard_withDueDateInPast_shouldReturnAssignment() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName("TestY");
        assignment.setLastCompletedAt(Instant.now().minus(10, ChronoUnit.DAYS)); // old completion
        assignment.setDoEveryNDays(5);

        when(psychologicalTestsAssignmentRepository.findActiveAssignments(eq(patient), any()))
                .thenReturn(List.of(assignment));

        // Act
        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        // Assert
        assertEquals(1, result.size());
        verify(authorizationService).checkPsychologicalTestAssignmentAccess(eq(assignment), eq(patient),
                eq("You do not have access to this patient's psychological tests."));
    }

    @Test
    void getPsychologicalTestsForDashboard_withDueDateNotReached_shouldReturnEmptyList() {
        // Arrange
        Patient patient = new Patient();
        patient.setId("p1");

        Instant recentCompletion = Instant.now().minus(1, ChronoUnit.DAYS);

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName("TestZ");
        assignment.setLastCompletedAt(recentCompletion); // not due yet
        assignment.setDoEveryNDays(7);

        when(psychologicalTestsAssignmentRepository.findActiveAssignments(eq(patient), any()))
                .thenReturn(List.of(assignment));

        // Act
        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        // Assert
        assertTrue(result.isEmpty());
        verify(authorizationService).checkPsychologicalTestAssignmentAccess(eq(assignment), eq(patient),
                eq("You do not have access to this patient's psychological tests."));
    }

    @Test
    void getPsychologicalTestsForDashboard_alreadyCompleted_shouldReturnNothing() {
        Patient patient = new Patient();
        patient.setId("p1");

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName("TestA");
        assignment.setLastCompletedAt(Instant.now());
        assignment.setDoEveryNDays(1);

        when(psychologicalTestsAssignmentRepository.findActiveAssignments(eq(patient), any()))
                .thenReturn(List.of(assignment));

        List<PsychologicalTestsOverviewOutputDTO> result = service.getPsychologicalTestsForDashboard(patient);

        assertEquals(0, result.size());
        verify(authorizationService).checkPsychologicalTestAssignmentAccess(eq(assignment), eq(patient),
                eq("You do not have access to this patient's psychological tests."));
    }

    @Test
    void getPsychologicalTestAssignment_exists_shouldReturnMappedDTO() {
        // Arrange
        String patientId = "p1";
        String testName = "TestA";

        Patient patient = new Patient();
        patient.setId(patientId);

        PsychologicalTestAssignment assignment = new PsychologicalTestAssignment();
        assignment.setPatient(patient);
        assignment.setTestName(testName);
        assignment.setDoEveryNDays(3);
        assignment.setIsPaused(false);
        assignment.setExerciseStart(Instant.now());
        assignment.setExerciseEnd(Instant.now().plus(3, ChronoUnit.DAYS));

        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, testName))
                .thenReturn(assignment);

        // Act
        PsychologicalTestAssignmentOutputDTO result = service.getPsychologicalTestAssginment(patientId, testName);

        // Assert
        assertEquals(patientId, result.getPatientId());
        assertEquals(testName, result.getTestName());
        assertEquals(3, result.getDoEveryNDays());
        assertFalse(result.getIsPaused());
    }

    @Test
    void getPsychologicalTestAssignment_notFound_shouldThrowAccessDeniedException() {
        // Arrange
        String patientId = "p1";
        String testName = "TestX";

        when(psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, testName))
                .thenReturn(null);

        // Act + Assert
        assertThrows(AccessDeniedException.class, () ->
                service.getPsychologicalTestAssginment(patientId, testName));
    }



}

