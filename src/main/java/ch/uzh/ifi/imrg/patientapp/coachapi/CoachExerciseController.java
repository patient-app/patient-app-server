package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExerciseInformationOutputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.ExercisesOverviewOutputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
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

    @GetMapping("/coach/patients/{patientId}/exercises")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ExercisesOverviewOutputDTO> getAllExercises(@PathVariable String patientId) {
        return exerciseService.getAllExercisesForCoach(patientId);
    }

    @PutMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void updateExercise(@PathVariable String patientId,
                               @PathVariable String exerciseId,
                               @RequestBody ExerciseInputDTO exerciseInputDTO){
        exerciseService.updateExercise(patientId, exerciseId, exerciseInputDTO);
    }

    @DeleteMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "X-Coach-Key")
    public void deleteExercise(@PathVariable String patientId,
                               @PathVariable String exerciseId) {
        exerciseService.deleteExercise(patientId, exerciseId);
    }

    @GetMapping("/coach/patients/{patientId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "X-Coach-Key")
    public List<ExerciseInformationOutputDTO> getExerciseInformation(@PathVariable String patientId,
                                                                     @PathVariable String exerciseId) {
        return exerciseService.getExerciseInformation(patientId, exerciseId);
    }

}
