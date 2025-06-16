package com.example.inventorypoc.service;

import com.example.inventorypoc.model.CurrentStock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PredictorServiceImplTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private PredictorServiceImpl predictorService;

    @Test
    void getPredictedStock_whenCurrentStockExists_shouldReturnPrediction() {
        Long productId = 1L;
        CurrentStock currentStock = new CurrentStock(productId, 100, LocalDateTime.now());
        when(inventoryService.getCurrentStock(productId)).thenReturn(Optional.of(currentStock));

        Optional<PredictorService.PredictedStockInfo> predictionOpt = predictorService.getPredictedStock(productId);

        assertTrue(predictionOpt.isPresent());
        PredictorService.PredictedStockInfo prediction = predictionOpt.get();
        assertEquals(productId, prediction.getProductId());
        assertEquals(100, prediction.getCurrentQuantity());
        assertEquals(110, prediction.getPredictedQuantity()); // 100 * 1.1
        assertEquals("Current stock (100) + 10%", prediction.getPredictionBasis());
    }

    @Test
    void getPredictedStock_whenNoCurrentStock_shouldReturnEmpty() {
        Long productId = 2L;
        when(inventoryService.getCurrentStock(productId)).thenReturn(Optional.empty());

        Optional<PredictorService.PredictedStockInfo> predictionOpt = predictorService.getPredictedStock(productId);

        assertFalse(predictionOpt.isPresent());
    }
}
