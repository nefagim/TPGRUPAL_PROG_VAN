package com.empresa.demostockapp.service.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PredictionService predictionService;

    private DemandPredictionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new DemandPredictionRequestDTO(1L, LocalDate.now().plusDays(1), 7);
    }

    @Test
    void predictDemand_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        DemandPredictionResponseDTO response = predictionService.predictDemand(requestDTO);

        assertNotNull(response);
        assertEquals(requestDTO.getProductId(), response.getProductId());
        assertEquals("placeholder_v0.1", response.getModelVersion());
        assertNotNull(response.getGeneratedAt());
        assertTrue(response.getGeneratedAt().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                   response.getGeneratedAt().isAfter(LocalDateTime.now().minusSeconds(5))); // Check if recent
        assertEquals(requestDTO.getNumberOfDaysToPredict(), response.getPredictedPoints().size());
        assertEquals(requestDTO.getNumberOfDaysToPredict(), response.getNumberOfDaysPredicted());


        for (int i = 0; i < requestDTO.getNumberOfDaysToPredict(); i++) {
            assertNotNull(response.getPredictedPoints().get(i).getDate());
            assertEquals(requestDTO.getPredictionStartDate().plusDays(i), response.getPredictedPoints().get(i).getDate());
            assertNotNull(response.getPredictedPoints().get(i).getPredictedValue());
            assertTrue(response.getPredictedPoints().get(i).getPredictedValue() >= 0 &&
                       response.getPredictedPoints().get(i).getPredictedValue() <= 100);
        }

        verify(productRepository, times(1)).existsById(1L);
    }

    @Test
    void predictDemand_productNotFound() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> predictionService.predictDemand(requestDTO));

        assertEquals("Product not found with id: " + requestDTO.getProductId(), exception.getMessage());
        verify(productRepository, times(1)).existsById(requestDTO.getProductId());
    }
}
