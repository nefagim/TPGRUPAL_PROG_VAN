package com.empresa.demostockapp.dto.predictions;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class DemandPredictionRequestDTO {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Prediction start date cannot be null")
    @FutureOrPresent(message = "Prediction start date must be today or in the future")
    private LocalDate predictionStartDate;

    @NotNull(message = "Number of days to predict cannot be null")
    @Min(value = 1, message = "Number of days to predict must be at least 1")
    private Integer numberOfDaysToPredict;

    public DemandPredictionRequestDTO() {
    }

    public DemandPredictionRequestDTO(Long productId, LocalDate predictionStartDate, Integer numberOfDaysToPredict) {
        this.productId = productId;
        this.predictionStartDate = predictionStartDate;
        this.numberOfDaysToPredict = numberOfDaysToPredict;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getPredictionStartDate() {
        return predictionStartDate;
    }

    public void setPredictionStartDate(LocalDate predictionStartDate) {
        this.predictionStartDate = predictionStartDate;
    }

    public Integer getNumberOfDaysToPredict() {
        return numberOfDaysToPredict;
    }

    public void setNumberOfDaysToPredict(Integer numberOfDaysToPredict) {
        this.numberOfDaysToPredict = numberOfDaysToPredict;
    }
}
