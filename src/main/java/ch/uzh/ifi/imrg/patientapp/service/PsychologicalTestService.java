package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestAssignment;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestRepository;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestsAssignmentRepository;
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
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class PsychologicalTestService {
    private final PsychologicalTestRepository psychologicalTestRepository;
    private final PsychologicalTestMapper psychologicalTestMapper;
    private final AuthorizationService authorizationService;
    private final PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository;

    public PsychologicalTestService(PsychologicalTestRepository psychologicalTestRepository, PsychologicalTestMapper psychologicalTestMapper, AuthorizationService authorizationService, PsychologicalTestsAssignmentRepository psychologicalTestsAssignmentRepository) {
        this.psychologicalTestRepository = psychologicalTestRepository;
        this.psychologicalTestMapper = psychologicalTestMapper;
        this.authorizationService = authorizationService;
        this.psychologicalTestsAssignmentRepository = psychologicalTestsAssignmentRepository;
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

        psychologicalTestRepository.save(psychologicalTest);
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

        return psychologicalTestMapper.convertEntityToPsychologicalTestAssignmentOverviewDTOs(dueAssignments);
    }

}
