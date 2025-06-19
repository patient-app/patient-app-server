package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.Exercise.CreateExerciseDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExerciseController {
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }
    @PostMapping("/coach/{patientId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public void createExercise(HttpServletRequest httpServletRequest, @PathVariable String patientId,
                               @RequestBody CreateExerciseDTO createExerciseDTO){
        //insert some auth functionality
        exerciseService.createExercise(patientId, createExerciseDTO);
    }

}
