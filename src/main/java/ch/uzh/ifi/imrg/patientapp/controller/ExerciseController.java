package ch.uzh.ifi.imrg.patientapp.controller;


import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.exercise.*;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ExerciseController {
    private final PatientService patientService;

    ExerciseController(PatientService patientService){
        this.patientService = patientService;
    }

    @GetMapping("/patients/exercises")
    @ResponseStatus(HttpStatus.OK)
    public List<ExercisesOverviewOutputDTO> getExerciseOverview(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        List<ExercisesOverviewOutputDTO> exercisesOverviewOutputDTOS = new ArrayList<>();
        ExercisesOverviewOutputDTO exercisesOverviewOutputDTO1 = new ExercisesOverviewOutputDTO();
        exercisesOverviewOutputDTO1.setName("Name of first exercise");
        exercisesOverviewOutputDTO1.setId("Id1");
        exercisesOverviewOutputDTO1.setPictureUrl("pictureUrlPlaceholder1");

        ExercisesOverviewOutputDTO exercisesOverviewOutputDTO2 = new ExercisesOverviewOutputDTO();
        exercisesOverviewOutputDTO2.setName("Name of second exercise");
        exercisesOverviewOutputDTO2.setId("Id2");
        exercisesOverviewOutputDTO2.setPictureUrl("pictureUrlPlaceholder2");
        exercisesOverviewOutputDTOS.add(exercisesOverviewOutputDTO1);
        exercisesOverviewOutputDTOS.add(exercisesOverviewOutputDTO2);
        return exercisesOverviewOutputDTOS;
    }


    @GetMapping("/patients/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseOutputDTO exerciseOutputDTO(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        ExerciseOutputDTO exerciseOutputDTO = new ExerciseOutputDTO();
        exerciseOutputDTO.setDescription("Solve the following exercise.");
        exerciseOutputDTO.setId("exercise-id");
        exerciseOutputDTO.setTitle("Psychotherapie exercise");

        List<ExerciseElementDTO> exerciseElementDTOList = new ArrayList<>();
        ExerciseImageElementDTO.ImageData imageData = new ExerciseImageElementDTO.ImageData();
        imageData.setAlt("Diagram A");
        imageData.setUrl("pictureUrl");

        ExerciseImageElementDTO exerciseImageElementDTO = new ExerciseImageElementDTO();
        exerciseImageElementDTO.setId("img-id");
        exerciseImageElementDTO.setType("IMAGE");
        exerciseImageElementDTO.setData(imageData);

        ExerciseFileElementDTO.FileData fileData = new ExerciseFileElementDTO.FileData();
        fileData.setName("file.pdf");
        fileData.setUrl("fileUrl");

        ExerciseFileElementDTO exerciseFileElementDTO = new ExerciseFileElementDTO();
        exerciseFileElementDTO.setId("File-id");
        exerciseFileElementDTO.setType("FILE");
        exerciseFileElementDTO.setData(fileData);

        ExerciseTextInputElementDTO.TextInputData textInputData = new ExerciseTextInputElementDTO.TextInputData();
        textInputData.setLabel("What are your thoughts?");
        textInputData.setPlaceholder("Your thoughts");
        textInputData.setRequired(true);

        ExerciseTextInputElementDTO exerciseTextInputElementDTO = new ExerciseTextInputElementDTO();
        exerciseTextInputElementDTO.setId("input-id");
        exerciseTextInputElementDTO.setType("TEXT_INPUT");
        exerciseTextInputElementDTO.setData(textInputData);


        exerciseElementDTOList.add(exerciseImageElementDTO);
        exerciseElementDTOList.add(exerciseFileElementDTO);
        exerciseElementDTOList.add(exerciseTextInputElementDTO);

        exerciseOutputDTO.setElements(exerciseElementDTOList);
        return exerciseOutputDTO;
    }
}
