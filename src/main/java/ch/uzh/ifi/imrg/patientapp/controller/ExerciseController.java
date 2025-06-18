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


    @GetMapping("/patients/exercises/")
    @ResponseStatus(HttpStatus.OK)
    public List<ExercisesOverviewOutputDTO> getExerciseOverview(HttpServletRequest httpServletRequest){
        Patient loggedInPatient = patientService.getCurrentlyLoggedInPatient(httpServletRequest);
        List<ExercisesOverviewOutputDTO> exercisesOverviewOutputDTOs = new ArrayList<>();
                exercisesOverviewOutputDTOs = exerciseService.getExercisesOverview(loggedInPatient);
        return exercisesOverviewOutputDTOs;
    }

    @GetMapping("/patients/exercises/mock")
    @ResponseStatus(HttpStatus.OK)
    public List<ExercisesOverviewOutputDTO> getExerciseOverviewMock(HttpServletRequest httpServletRequest){
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


    @GetMapping("/patients/exercises/{exerciseId}/mock")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseOutputDTO GetExerciseOutputDTOMock(HttpServletRequest httpServletRequest){
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
    @GetMapping("/patients/exercises/{exerciseId}/pictures/{pictureId}/mock")
    public ResponseEntity<ClassPathResource> getPictureMock(
            @PathVariable String exerciseId,
            @PathVariable String pictureId) throws IOException {

        // For now, we ignore the actual IDs and just serve a static image
        ClassPathResource imgFile = new ClassPathResource("placeholder.jpg");

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imgFile);
    }

    @GetMapping("/patients/exercises/{exerciseId}/documents/{fileId}/mock")
    public ResponseEntity<ClassPathResource> getPdfMock(
            @PathVariable String exerciseId,
            @PathVariable String fileId) throws IOException {

        ClassPathResource pdfFile = new ClassPathResource("placeholder.pdf");

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfFile);
    }

}
