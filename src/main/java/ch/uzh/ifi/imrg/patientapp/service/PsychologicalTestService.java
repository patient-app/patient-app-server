package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.constant.AvailableTests;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestsAssignmentRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestAssignmentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameAndPatientIdOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestNameOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestsOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PsychologicalTestMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class PsychologicalTestService {
    private final PsychologicalTestRepository psychologicalTestRepository;
    private final AuthorizationService authorizationService;
    private final PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository;
    private final PatientRepository patientRepository;
    private final LogService logService;

    public PsychologicalTestService(PsychologicalTestRepository psychologicalTestRepository, AuthorizationService authorizationService, PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository, PatientRepository patientRepository, LogService logService) {
        this.psychologicalTestRepository = psychologicalTestRepository;
        this.authorizationService = authorizationService;
        this.psychologicalTestsAssignmentRepository = psychologicalTestsAssignmentRepository;
        this.patientRepository = patientRepository;
        this.logService = logService;
    }

    public void createPsychologicalTestAssginment(String patientId, String psychologicalTestName, PsychologicalTestAssignmentInputDTO psychologicalTestAssignmentInputDTO) {
        Patient patient = patientRepository.getPatientById(patientId);

        PsychologicalTestAssignment psychologicalTestAssignment = PsychologicalTestMapper.INSTANCE.convertPsychologicalTestAssignmentInputDTOToPsychologicalTestAssignment(psychologicalTestAssignmentInputDTO);
        psychologicalTestAssignment.setTestName(psychologicalTestName);
        psychologicalTestAssignment.setPatient(patient);
        psychologicalTestsAssignmentRepository.save(psychologicalTestAssignment);
    }

    public void updatePsychologicalTestAssginment(String patientId, String psychologicalTestName, PsychologicalTestAssignmentInputDTO psychologicalTestAssignmentInputDTO) {
        Patient patient = patientRepository.getPatientById(patientId);
        PsychologicalTestAssignment psychologicalTestAssignment = psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(patientId, psychologicalTestName);
        if (psychologicalTestAssignment == null) {
            throw new AccessDeniedException("No psychological test assignment found for the given patient and test name.");
        }
        authorizationService.checkPsychologicalTestAssignmentAccess(psychologicalTestAssignment, patient, "You do not have access to this patient's psychological test assignment.");
        PsychologicalTestMapper.INSTANCE.updatePsychologicalTestAssignmentFromDTO(psychologicalTestAssignmentInputDTO, psychologicalTestAssignment);

        psychologicalTestsAssignmentRepository.save(psychologicalTestAssignment);
    }

    public void createPsychologicalTest(Patient loggedInPatient, PsychologicalTestInputDTO psychologicalTestInputDTO) {
        PsychologicalTest psychologicalTest = PsychologicalTestMapper.INSTANCE.convertPsychologicalTestInputDTOToPsychologicalTest(psychologicalTestInputDTO);

        psychologicalTest.setPatient(loggedInPatient);

        // Set back-references
        if (psychologicalTest.getPsychologicalTestsQuestions() != null) {
            for (PsychologicalTestQuestions question : psychologicalTest.getPsychologicalTestsQuestions()) {
                question.setPsychologicalTest(psychologicalTest);
            }
        }
        PsychologicalTestAssignment psychologicalTestAssignment = psychologicalTestsAssignmentRepository.findByPatientIdAndTestName(loggedInPatient.getId(), psychologicalTestInputDTO.getName());
        if (psychologicalTestAssignment == null) {
            psychologicalTestAssignment = new PsychologicalTestAssignment();
            psychologicalTestAssignment.setPatient(loggedInPatient);
            psychologicalTestAssignment.setTestName(psychologicalTestInputDTO.getName());
            psychologicalTestAssignment.setDoEveryNDays(1000);
            psychologicalTestAssignment.setIsPaused(false);
        } else {
            authorizationService.checkPsychologicalTestAssignmentAccess(psychologicalTestAssignment, loggedInPatient, "You do not have access to this patient's psychological test assignment.");
        }

        psychologicalTestRepository.save(psychologicalTest);
        psychologicalTestAssignment.setLastCompletedAt(Instant.now());
        psychologicalTestsAssignmentRepository.save(psychologicalTestAssignment);

        logService.createLog(loggedInPatient.getId(), LogTypes.PSYCHOLOGICAL_TEST_COMPLETED, psychologicalTest.getName(),"" );
    }

    public List<PsychologicalTestNameOutputDTO> getAllTestNamesForPatientForCoach(String patientId) {
        return psychologicalTestRepository.findAllByPatientId(patientId);
    }

    public List<PsychologicalTestNameAndPatientIdOutputDTO> getAllTestNamesForPatient(Patient loggedInPatient, String patientId) {
        List <PsychologicalTestNameAndPatientIdOutputDTO> psychologicalTestNames = psychologicalTestRepository.findAllTestsByPatientId(patientId);
        if (!psychologicalTestNames.isEmpty()) {
            if (!psychologicalTestNames.getFirst().getPatientId().equals(loggedInPatient.getId())) {
                throw new AccessDeniedException("You do not have access to this patient's psychological tests.");
            }
        }
        return psychologicalTestNames;
    }

    public List<PsychologicalTestNameOutputDTO> getAllAvailableTestNamesForPatientForCoach(String patientId) {
        return Arrays.stream(AvailableTests.values())
                .map(test -> new PsychologicalTestNameOutputDTO(test.name()))
                .toList();
    }

    public List<PsychologicalTestOutputDTO> getPsychologicalTestResultsForCoach(String patientId, String psychologicalTestName) {
        return psychologicalTestRepository.findAllByPatientIdAndName(patientId, psychologicalTestName)
                .stream()
                .map(PsychologicalTestMapper.INSTANCE::convertPsychologicalTestToPsychologicalTestOutputDTO)
                .toList();
    }

    public List<PsychologicalTestOutputDTO> getPsychologicalTestResults(Patient loggedInPatient ,String patientId, String psychologicalTestName) {
        List <PsychologicalTestOutputDTO> psychologicalTestOutputDTOs = psychologicalTestRepository.findAllByPatientIdAndName(patientId, psychologicalTestName)
                .stream()
                .map(
                PsychologicalTestMapper.INSTANCE::convertPsychologicalTestToPsychologicalTestOutputDTO
        ).toList();

        if (!psychologicalTestOutputDTOs.isEmpty()) {
            if (!psychologicalTestOutputDTOs.getFirst().getPatientId().equals(loggedInPatient.getId())) {
                throw new AccessDeniedException("You do not have access to this patient's psychological tests.");
            }
        }

        return psychologicalTestOutputDTOs;
    }

    public List<PsychologicalTestsOverviewOutputDTO> getPsychologicalTestsForDashboard(Patient patient) {
        Instant now = Instant.now();
        List<PsychologicalTestAssignment> assignments = psychologicalTestsAssignmentRepository.findActiveAssignments(patient, now);

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        authorizationService.checkPsychologicalTestAssignmentAccess(assignments.getFirst(), patient, "You do not have access to this patient's psychological tests.");

        List<PsychologicalTestAssignment> dueAssignments = assignments.stream()
                .filter(a -> {
                    Instant lastCompleted = a.getLastCompletedAt();
                    if (lastCompleted == null) {
                        return true;
                    }

                    Instant dueDate = lastCompleted.plus(a.getDoEveryNDays(), ChronoUnit.DAYS);
                    Instant threshold = dueDate.minus(24, ChronoUnit.HOURS);

                    return now.isAfter(threshold);
                })
                .toList();

        return PsychologicalTestMapper.INSTANCE.convertEntityToPsychologicalTestAssignmentOverviewDTOs(dueAssignments);
    }

}
