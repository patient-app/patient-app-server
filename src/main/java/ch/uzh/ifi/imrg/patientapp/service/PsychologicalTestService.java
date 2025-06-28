package ch.uzh.ifi.imrg.patientapp.service;


import ch.uzh.ifi.imrg.patientapp.entity.Exercise.Exercise;
import ch.uzh.ifi.imrg.patientapp.entity.Exercise.ExerciseElement;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;
import ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTestQuestions;
import ch.uzh.ifi.imrg.patientapp.repository.PsychologicalTestRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PsychologicalTestInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.exercise.ExerciseInputDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.PsychologicalTestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PsychologicalTestService {
    private final PsychologicalTestRepository psychologicalTestRepository;

    public PsychologicalTestService(PsychologicalTestRepository psychologicalTestRepository) {
        this.psychologicalTestRepository = psychologicalTestRepository;
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

}
