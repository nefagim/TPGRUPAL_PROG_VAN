package com.empresa.demostockapp.controller.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.dto.predictions.PredictedDemandPointDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.predictions.PredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(PredictionController.class)
@WithMockUser(roles = "MANAGER") // Or "ADMIN", as per WebSecurityConfig
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictionService predictionService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // Needed for WebMvcTest with security

    @MockBean
    private JwtUtils jwtUtils; // Needed for WebMvcTest with security

    @Autowired
    private ObjectMapper objectMapper;

    private DemandPredictionRequestDTO validRequestDTO;
    private DemandPredictionResponseDTO sampleResponseDTO;

    @BeforeEach
    void setUp() {
        validRequestDTO = new DemandPredictionRequestDTO(1L, LocalDate.now().plusDays(1), 5);

        List<PredictedDemandPointDTO> points = Collections.singletonList(
                new PredictedDemandPointDTO(LocalDate.now().plusDays(1), 50.0)
        );
        sampleResponseDTO = new DemandPredictionResponseDTO(
                1L,
                LocalDate.now().plusDays(1),
                1, // For simplicity, just one point in this sample
                points,
                "placeholder_v0.1",
                LocalDateTime.now()
        );
    }

    @Test
    void getDemandPrediction_success() throws Exception {
        when(predictionService.predictDemand(any(DemandPredictionRequestDTO.class))).thenReturn(sampleResponseDTO);

        mockMvc.perform(post("/api/predictions/demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.modelVersion", is("placeholder_v0.1")))
                .andExpect(jsonPath("$.predictedPoints", hasSize(1)));
    }

    @Test
    void getDemandPrediction_badRequest_invalidInput_nullProductId() throws Exception {
        DemandPredictionRequestDTO invalidRequest = new DemandPredictionRequestDTO(null, LocalDate.now(), 5);

        // No need to mock service, as validation should prevent service call
        mockMvc.perform(post("/api/predictions/demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400 due to @NotNull on productId
    }

    @Test
    void getDemandPrediction_badRequest_invalidInput_pastDate() throws Exception {
        DemandPredictionRequestDTO invalidRequest = new DemandPredictionRequestDTO(1L, LocalDate.now().minusDays(1), 5);

        mockMvc.perform(post("/api/predictions/demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400 due to @FutureOrPresent
    }

    @Test
    void getDemandPrediction_badRequest_invalidInput_zeroDays() throws Exception {
        DemandPredictionRequestDTO invalidRequest = new DemandPredictionRequestDTO(1L, LocalDate.now().plusDays(1), 0);

        mockMvc.perform(post("/api/predictions/demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400 due to @Min(1)
    }


    @Test
    void getDemandPrediction_productNotFound() throws Exception {
        when(predictionService.predictDemand(any(DemandPredictionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: " + validRequestDTO.getProductId()));

        mockMvc.perform(post("/api/predictions/demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: " + validRequestDTO.getProductId()));
    }
}
