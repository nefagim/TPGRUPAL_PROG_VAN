package com.empresa.demostockapp.service.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.dto.predictions.PredictedDemandPointDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PredictionService {

    private final ProductRepository productRepository;
    private final Random random = new Random();

    public PredictionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public DemandPredictionResponseDTO predictDemand(DemandPredictionRequestDTO requestDTO) {
        if (!productRepository.existsById(requestDTO.getProductId())) {
            throw new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId());
        }

        List<PredictedDemandPointDTO> predictedPoints = new ArrayList<>();
        LocalDate currentDate = requestDTO.getPredictionStartDate();
        for (int i = 0; i < requestDTO.getNumberOfDaysToPredict(); i++) {
            double predictedValue = Math.round((random.nextDouble() * 100.0) * 100.0) / 100.0; // Random value, rounded to 2 decimal places
            predictedPoints.add(new PredictedDemandPointDTO(currentDate, predictedValue));
            currentDate = currentDate.plusDays(1);
        }

        return new DemandPredictionResponseDTO(
                requestDTO.getProductId(),
                requestDTO.getPredictionStartDate(),
                predictedPoints.size(),
                predictedPoints,
                "placeholder_v0.1",
                LocalDateTime.now()
        );
    }
}
