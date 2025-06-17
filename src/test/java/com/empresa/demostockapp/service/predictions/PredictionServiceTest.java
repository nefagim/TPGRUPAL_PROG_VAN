package com.empresa.demostockapp.service.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.dto.predictions.PredictedDemandPointDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.SalesOrderRepository; // Added
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository; // Added

    @InjectMocks
    private PredictionService predictionService;

    private DemandPredictionRequestDTO requestDTO;
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Test Product", "Desc", BigDecimal.TEN, "SKU001");
        product.setId(1L);
        requestDTO = new DemandPredictionRequestDTO(1L, LocalDate.now().plusDays(1), 5);
    }

    @Test
    void predictDemand_success_withHistoricalData() {
        when(productRepository.existsById(1L)).thenReturn(true);

        List<SalesOrder> historicalSales = new ArrayList<>();
        // Total 90 sales in 90 days period for an average of 1.0
        historicalSales.add(new SalesOrder(product, 30, BigDecimal.TEN));
        historicalSales.add(new SalesOrder(product, 45, BigDecimal.TEN));
        historicalSales.add(new SalesOrder(product, 15, BigDecimal.TEN));
        // Mock dates for these if needed, but service logic doesn't use individual sale dates for calculation, only the sum.

        when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(historicalSales);

        DemandPredictionResponseDTO response = predictionService.predictDemand(requestDTO);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(5, response.getPredictedPoints().size());
        assertEquals("average_daily_sales_v1.0_90day_window", response.getModelVersion());

        double expectedAverage = 90.0 / 90.0; // 1.0
        for (PredictedDemandPointDTO point : response.getPredictedPoints()) {
            assertEquals(expectedAverage, point.getPredictedValue(), 0.001); // Using delta for double comparison
        }
        verify(productRepository, times(1)).existsById(1L);
        verify(salesOrderRepository, times(1)).findByProductIdAndOrderDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void predictDemand_success_noHistoricalData() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        DemandPredictionResponseDTO response = predictionService.predictDemand(requestDTO);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(5, response.getPredictedPoints().size());
        assertEquals("average_daily_sales_v1.0_no_historical_data", response.getModelVersion());

        for (PredictedDemandPointDTO point : response.getPredictedPoints()) {
            assertEquals(0.0, point.getPredictedValue(), 0.001);
        }
        verify(productRepository, times(1)).existsById(1L);
        verify(salesOrderRepository, times(1)).findByProductIdAndOrderDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void predictDemand_success_historicalData_totalQuantityZero() {
        // This case tests when there are sales orders but their quantities sum to zero (e.g. returns processed as negative sales, though current model doesn't support that)
        // Or more realistically, if SalesOrder.quantitySold could be 0 (which @Positive prevents).
        // For now, if totalQuantitySold is 0, it should behave like no historical data for prediction value.
        when(productRepository.existsById(1L)).thenReturn(true);
        List<SalesOrder> historicalSalesWithZeroSum = new ArrayList<>();
        // Add SalesOrder that might sum to zero if negative quantities were allowed or if all quantities are zero
        // Since SalesOrder quantitySold is @Positive, this means the list is empty or all sales are for other products (filtered by findByProductId...)
        // The current logic: if totalQuantitySold > 0 is false, then average is 0.
        // So, if historicalSales is not empty, but totalQuantitySold is 0 (not possible with @Positive), it would use 0.
        // Let's test with an empty list, which leads to totalQuantitySold = 0. This is covered by predictDemand_success_noHistoricalData.
        // What if list is not empty, but somehow (e.g. data issue) totalQuantitySold = 0?
        // The code is: if (!historicalSales.isEmpty() && totalQuantitySold > 0)
        // So if totalQuantitySold is 0, even if list not empty, it goes to else.

        // Let's adjust to a more realistic scenario based on current constraints:
        // One sale of 0 items is not possible due to @Positive.
        // So this case defaults to "no historical data" if the list is empty.
        // If the list is NOT empty, but totalQuantitySold is somehow 0 (which current constraints prevent),
        // it would result in modelVersion = "average_daily_sales_v1.0_no_historical_data" and prediction 0.0.
        // This is effectively the same as noHistoricalData.

        // Let's test the rounding for a small average
         when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new SalesOrder(product, 1, BigDecimal.TEN))); // 1 sale in 90 days

        DemandPredictionResponseDTO response = predictionService.predictDemand(requestDTO);
        assertNotNull(response);
        assertEquals("average_daily_sales_v1.0_90day_window", response.getModelVersion());
        double expectedAverage = Math.round((1.0/90.0)*100.0)/100.0; //0.01
         for (PredictedDemandPointDTO point : response.getPredictedPoints()) {
            assertEquals(expectedAverage, point.getPredictedValue(), 0.001);
        }
    }


    @Test
    void predictDemand_success_oneHistoricalSale() {
        when(productRepository.existsById(1L)).thenReturn(true);
        List<SalesOrder> historicalSales = Collections.singletonList(new SalesOrder(product, 9, BigDecimal.TEN));
        when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(historicalSales);

        DemandPredictionResponseDTO response = predictionService.predictDemand(requestDTO);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(5, response.getPredictedPoints().size());
        assertEquals("average_daily_sales_v1.0_90day_window", response.getModelVersion());

        double expectedAverage = 9.0 / 90.0; // 0.1
        double roundedExpectedAverage = Math.round(expectedAverage * 100.0) / 100.0;

        for (PredictedDemandPointDTO point : response.getPredictedPoints()) {
            assertEquals(roundedExpectedAverage, point.getPredictedValue(), 0.001);
        }
        verify(productRepository, times(1)).existsById(1L);
        verify(salesOrderRepository, times(1)).findByProductIdAndOrderDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void predictDemand_productNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> predictionService.predictDemand(requestDTO));

        assertEquals("Product not found with id: " + requestDTO.getProductId(), exception.getMessage());
        verify(productRepository, times(1)).existsById(requestDTO.getProductId());
        // Ensure sales order repo is not called if product not found
        verify(salesOrderRepository, never()).findByProductIdAndOrderDateBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
