package com.example.inventorypoc.controller;

import com.example.inventorypoc.service.PredictorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(PredictorController.class)
public class PredictorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictorService predictorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPredictedStock_whenPredictionExists_shouldReturnPrediction() throws Exception {
        Long productId = 1L;
        PredictorService.PredictedStockInfo prediction = new PredictorService.PredictedStockInfo(productId, 100, 110, "Test Basis");
        when(predictorService.getPredictedStock(productId)).thenReturn(Optional.of(prediction));

        mockMvc.perform(get("/api/predictor/stock/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.currentQuantity", is(100)))
                .andExpect(jsonPath("$.predictedQuantity", is(110)));
    }

    @Test
    void getPredictedStock_whenNoPrediction_shouldReturnNotFound() throws Exception {
        Long productId = 2L;
        when(predictorService.getPredictedStock(productId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/predictor/stock/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
