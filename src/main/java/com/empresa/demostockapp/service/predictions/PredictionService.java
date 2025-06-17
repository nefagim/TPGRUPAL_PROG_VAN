package com.empresa.demostockapp.service.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.dto.predictions.PredictedDemandPointDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// No longer need java.util.Random

@Service
public class PredictionService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository; // Added

    // Updated constructor
    public PredictionService(ProductRepository productRepository, SalesOrderRepository salesOrderRepository) {
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    public DemandPredictionResponseDTO predictDemand(DemandPredictionRequestDTO requestDTO) {
        if (!productRepository.existsById(requestDTO.getProductId())) {
            throw new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId());
        }

        // Define historical data window
        LocalDateTime historyEndDate = requestDTO.getPredictionStartDate().atStartOfDay();
        LocalDateTime historyStartDate = historyEndDate.minusDays(90);

        // Fetch historical sales
        List<SalesOrder> historicalSales = salesOrderRepository.findByProductIdAndOrderDateBetween(
                requestDTO.getProductId(), historyStartDate, historyEndDate);

        // Calculate average daily sales
        double totalQuantitySold = historicalSales.stream().mapToInt(SalesOrder::getQuantitySold).sum();
        double averageDailySales = 0.0;
        String modelVersion;

        if (!historicalSales.isEmpty() && totalQuantitySold > 0) {
            // Calculate the actual number of days in the historical window for a more precise average
            // However, the requirement was to divide by 90.0, implying a fixed window size for averaging.
            // If a product had sales only on 1 day out of 90, average would be total/90.
            averageDailySales = totalQuantitySold / 90.0;
            modelVersion = "average_daily_sales_v1.0_90day_window";
        } else {
             // If no sales, or product is new, averageDailySales remains 0.0
            modelVersion = "average_daily_sales_v1.0_no_historical_data";
        }

        // Generate predicted points
        List<PredictedDemandPointDTO> predictedPoints = new ArrayList<>();
        LocalDate currentDate = requestDTO.getPredictionStartDate();
        for (int i = 0; i < requestDTO.getNumberOfDaysToPredict(); i++) {
            // Round averageDailySales to a reasonable number of decimal places, e.g., 2
            double roundedPrediction = Math.round(averageDailySales * 100.0) / 100.0;
            predictedPoints.add(new PredictedDemandPointDTO(currentDate.plusDays(i), roundedPrediction));
        }

        return new DemandPredictionResponseDTO(
                requestDTO.getProductId(),
                requestDTO.getPredictionStartDate(),
                predictedPoints.size(),
                predictedPoints,
                modelVersion, // Updated modelVersion
                LocalDateTime.now()
        );
    }
}
