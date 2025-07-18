package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseCompletionNameInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentResultInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInformationInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<ExecutionOverviewOutputDTO> getOneExerciseOverview(HttpServletRequest httpServletRequest,
                                                                  @PathVariable String exerciseId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getOneExercisesOverview(loggedInPatient, exerciseId);
    }

    @GetMapping("/patients/exercises/{exerciseId}/execution-information/{exerciseExecutionId}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseOutputDTO getExerciseExecution(HttpServletRequest httpServletRequest,
                                                  @PathVariable String exerciseId,
                                                  @PathVariable String exerciseExecutionId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExerciseExecution(exerciseId, loggedInPatient, exerciseExecutionId);
    }

    @PostMapping("/patients/exercises/{exerciseId}/start")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseStartOutputDTO startExercise(HttpServletRequest httpServletRequest,
                                         @PathVariable String exerciseId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.startExercise(exerciseId, loggedInPatient);
    }

    @PostMapping("/patients/exercises/{exerciseId}/exercise-completed")
    @ResponseStatus(HttpStatus.OK)
    public void postExerciseFeedback(HttpServletRequest httpServletRequest,
            @PathVariable String exerciseId,
            @RequestBody ExerciseInformationInputDTO exerciseInformationInputDTO) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        exerciseService.putExerciseFeedback(loggedInPatient, exerciseId, exerciseInformationInputDTO);
    }

    @PostMapping("/patients/exercises/{exerciseId}/exercise-completion-name")
    @ResponseStatus(HttpStatus.OK)
    public void postExerciseCompletionName(HttpServletRequest httpServletRequest,
                                     @PathVariable String exerciseId,
                                     @RequestBody ExerciseCompletionNameInputDTO exerciseCompletionNameInputDTO) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        exerciseService.setExerciseCompletionName(loggedInPatient, exerciseId, exerciseCompletionNameInputDTO);
    }

    @PutMapping("/patients/exercises/{exerciseId}/exercise-completion-name")
    @ResponseStatus(HttpStatus.OK)
    public void putExerciseCompletionName(HttpServletRequest httpServletRequest,
                                     @PathVariable String exerciseId,
                                     @RequestBody ExerciseCompletionNameInputDTO exerciseCompletionNameInputDTO) {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        exerciseService.setExerciseCompletionName(loggedInPatient, exerciseId,  exerciseCompletionNameInputDTO);
    }



    @PostMapping("/patients/exercises/{exerciseId}/exercise-components/{exerciseComponentId}")
    @ResponseStatus(HttpStatus.OK)
    public void postExerciseComponentResult(HttpServletRequest httpServletRequest,
                                            @PathVariable String exerciseId,
                                            @PathVariable String exerciseComponentId,
                                            @RequestBody ExerciseComponentResultInputDTO exerciseComponentResultInputDTO) throws Exception {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        exerciseService.setExerciseComponentResult(loggedInPatient, exerciseId, exerciseComponentResultInputDTO, exerciseComponentId);
    }

    @PutMapping("/patients/exercises/{exerciseId}/exercise-components/{exerciseComponentId}")
    @ResponseStatus(HttpStatus.OK)
    public void putExerciseComponentResult(HttpServletRequest httpServletRequest,
                                            @PathVariable String exerciseId,
                                            @PathVariable String exerciseComponentId,
                                            @RequestBody ExerciseComponentResultInputDTO exerciseComponentResultInputDTO) throws Exception {
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        exerciseService.setExerciseComponentResult(loggedInPatient, exerciseId, exerciseComponentResultInputDTO, exerciseComponentId);
    }
    @GetMapping("/patients/exercises/{exerciseId}/chatbot")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseChatbotOutputDTO getExerciseChatbot(HttpServletRequest httpServletRequest,
                                                       @PathVariable String exerciseId){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        return exerciseService.getExerciseChatbot(exerciseId, loggedInPatient);
    }
}
