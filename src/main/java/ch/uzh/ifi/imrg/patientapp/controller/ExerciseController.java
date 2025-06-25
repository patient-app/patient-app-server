package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ExerciseController {
    private final PatientService patientService;
    private final ExerciseService exerciseService;

    ExerciseController(PatientService patientService, ExerciseService exerciseService) {
        this.patientService = patientService;
        this.exerciseService = exerciseService;
    }


    @GetMapping("/patients/exercises")
    @ResponseStatus(HttpStatus.OK)
    public List<ExercisesOverviewOutputDTO> getExerciseOverview(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExercisesOverview(loggedInPatient);
    }


    @GetMapping("/patients/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseOutputDTO GetExerciseOutputDTOMock(HttpServletRequest httpServletRequest,
                                                      @PathVariable String exerciseId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExercise(exerciseId);
    }
    @GetMapping("/patients/exercises/{exerciseId}/{mediaId}")
    public ExerciseMediaOutputDTO getPictureMock(HttpServletRequest httpServletRequest,
            @PathVariable String exerciseId,
            @PathVariable String mediaId) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExerciseMedia(loggedInPatient, exerciseId, mediaId);

    }

}
