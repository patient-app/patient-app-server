package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseComponentInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CoachExerciseController {
    private final ExerciseService exerciseService;

    public CoachExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }


    @PostMapping("/coach/patients/{patientId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public void createExercise(@PathVariable String patientId,
                               @RequestBody ExerciseInputDTO exerciseInputDTO){
        exerciseService.createExercise(patientId, exerciseInputDTO);
    }

    @PostMapping("/coach/patients/{patientId}/exercises/{exerciseId}/exercise-components")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "X-Coach-Key")
    public void createExerciseComponent(@PathVariable String patientId,
                                        @PathVariable String exerciseId,
                                        @RequestBody ExerciseComponentInputDTO exerciseComponentInputDTO){
        exerciseService.createExerciseComponent(patientId, exerciseId, exerciseComponentInputDTO);
    }

    @GetMapping("/coach/patients/{patientId}/exercises")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ExercisesOverviewOutputDTO> getAllExercises(@PathVariable String patientId) {
        return exerciseService.getAllExercisesForCoach(patientId);
    }

    @GetMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ExercisesOverviewOutputDTO> getAllExerciseComponents(@PathVariable String patientId,
                                                                     @PathVariable String exerciseId) {
        return exerciseService.getAllExercisesComponentsOfAnExerciseForCoach(patientId,exerciseId);
    }
    @PutMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void updateExercise(@PathVariable String patientId,
                               @PathVariable String exerciseId,
                               @RequestBody ExerciseInputDTO exerciseInputDTO){
        exerciseService.updateExercise(patientId, exerciseId, exerciseInputDTO);
    }

    @PutMapping("/coach/patients/{patientId}/exercises/{exerciseId}/exercise-components/{exerciseComponentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void updateExerciseComponent(@PathVariable String patientId,
                               @PathVariable String exerciseId,
                               @PathVariable String exerciseComponentId,
                               @RequestBody ExerciseInputDTO exerciseInputDTO){
        exerciseService.updateExerciseComponent(patientId, exerciseId, exerciseInputDTO, exerciseComponentId);
    }
    @DeleteMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deleteExercise(@PathVariable String patientId,
                               @PathVariable String exerciseId) {
        exerciseService.deleteExercise(patientId, exerciseId);
    }
    @DeleteMapping("/coach/patients/{patientId}/exercises/{exerciseId}/exercise-components/{exerciseComponentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deleteExerciseComponent(@PathVariable String patientId,
                                        @PathVariable String exerciseId,
                                        @PathVariable String exerciseComponentId) {
        exerciseService.deleteExerciseComponent(patientId, exerciseId, exerciseComponentId);
    }

    @GetMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ExerciseInformationOutputDTO> getExerciseInformation(@PathVariable String patientId,
                                                                     @PathVariable String exerciseId) {
        return exerciseService.getExerciseInformation(patientId, exerciseId);
    }

}
