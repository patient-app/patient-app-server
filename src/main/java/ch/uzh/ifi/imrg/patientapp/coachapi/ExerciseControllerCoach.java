package ch.uzh.ifi.imrg.patientapp.coachapi;

import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.service.ExerciseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExerciseControllerCoach {
    private final ExerciseService exerciseService;

    public ExerciseControllerCoach(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }
    @PostMapping("/coach/{patientId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public void createExercise(HttpServletRequest httpServletRequest, @PathVariable String patientId,
                               @RequestBody ExerciseInputDTO exerciseInputDTO){
        //insert some auth functionality evtl. something to directly pass the patient.
        exerciseService.createExercise(patientId, exerciseInputDTO);
    }

}
