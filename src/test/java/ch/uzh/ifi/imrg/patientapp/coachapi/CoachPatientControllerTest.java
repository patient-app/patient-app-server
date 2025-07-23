package ch.uzh.ifi.imrg.patientapp.coachapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.CreatePatientDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.UpdateCoachEmailDTO;
import ch.uzh.ifi.imrg.patientapp.service.PatientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CoachPatientControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private PatientService patientService;

        @InjectMocks
        private CoachPatientController controller;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(controller)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build();
        }

        @Test
        void registerPatient_shouldReturnCreatedPatient() throws Exception {
                // prepare input DTO
                CreatePatientDTO input = new CreatePatientDTO();
                input.setEmail("alice@example.com");
                input.setPassword("Password1");
                input.setCoachAccessKey("coach-key-123");

                // prepare returned entity
                Patient saved = new Patient();
                saved.setId("p1");
                saved.setEmail("alice@example.com");
                saved.setPassword("Password1.");
                saved.setCoachAccessKey("coach-key-123");

                when(patientService.registerPatient(
                                any(Patient.class),
                                any(HttpServletRequest.class),
                                any(HttpServletResponse.class)))
                                .thenReturn(saved);

                mockMvc.perform(post("/coach/patients/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Coach-Key", "coach-key-123")
                                .content(objectMapper.writeValueAsString(input)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("p1"))
                                .andExpect(jsonPath("$.email").value("alice@example.com"));
        }

        @Test
        void deletePatient_shouldReturnNoContent() throws Exception {
                String patientId = "p1";

                doNothing().when(patientService).removePatient(patientId);

                mockMvc.perform(delete("/coach/patients/{patientId}", patientId))
                                .andExpect(status().isNoContent());

                verify(patientService).removePatient(patientId);
        }

        @Test
        void setCoachEmail_shouldReturnNoContentAndInvokeService() throws Exception {
                String patientId = "p1";

                // prepare DTO
                UpdateCoachEmailDTO dto = new UpdateCoachEmailDTO();
                dto.setCoachEmail("coach@example.com");

                // stub service
                doNothing().when(patientService).updateCoachEmail(patientId, dto.getCoachEmail());

                // exercise endpoint
                mockMvc.perform(put("/coach/patients/{patientId}/coach-email", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isNoContent());

                // verify service call
                verify(patientService).updateCoachEmail(patientId, dto.getCoachEmail());
        }
}
