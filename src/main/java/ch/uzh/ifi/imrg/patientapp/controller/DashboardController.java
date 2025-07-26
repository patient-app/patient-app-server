package ch.uzh.ifi.imrg.patientapp.controller;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.PsychologicalTestsOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import ch.uzh.ifi.imrg.patientapp.service.PsychologicalTestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardController {
    private final PatientService patientService;
    private final ExerciseService exerciseService;
    private final PsychologicalTestService psychologicalTestService;

    DashboardController(PatientService patientService, ExerciseService exerciseService, PsychologicalTestService psychologicalTestService) {
        this.patientService = patientService;
        this.exerciseService = exerciseService;
        this.psychologicalTestService = psychologicalTestService;
    }

    @GetMapping("/patients/dashboard/exercises")
    @ResponseStatus(HttpStatus.OK)
    public List<ExercisesOverviewOutputDTO> getExerciseOverview(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExercisesForDashboard(loggedInPatient);
    }

    @GetMapping("/patients/dashboard/psychological-tests")
    @ResponseStatus(HttpStatus.OK)
    public List<PsychologicalTestsOverviewOutputDTO> getPsychologicalTestsOverview(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return psychologicalTestService.getPsychologicalTestsForDashboard(loggedInPatient);
    }

}
