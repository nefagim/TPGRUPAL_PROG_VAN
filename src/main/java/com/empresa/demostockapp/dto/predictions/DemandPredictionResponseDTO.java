package com.empresa.demostockapp.dto.predictions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DemandPredictionResponseDTO {

    private Long productId;
    private LocalDate predictionStartDate;
    private Integer numberOfDaysPredicted;
    private List<PredictedDemandPointDTO> predictedPoints;
    private String modelVersion;
    private LocalDateTime generatedAt;

    public DemandPredictionResponseDTO() {
    }

    public DemandPredictionResponseDTO(Long productId, LocalDate predictionStartDate, Integer numberOfDaysPredicted,
                                     List<PredictedDemandPointDTO> predictedPoints, String modelVersion,
                                     LocalDateTime generatedAt) {
        this.productId = productId;
        this.predictionStartDate = predictionStartDate;
        this.numberOfDaysPredicted = numberOfDaysPredicted;
        this.predictedPoints = predictedPoints;
        this.modelVersion = modelVersion;
        this.generatedAt = generatedAt;
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

    public Integer getNumberOfDaysPredicted() {
        return numberOfDaysPredicted;
    }

    public void setNumberOfDaysPredicted(Integer numberOfDaysPredicted) {
        this.numberOfDaysPredicted = numberOfDaysPredicted;
    }

    public List<PredictedDemandPointDTO> getPredictedPoints() {
        return predictedPoints;
    }

    public void setPredictedPoints(List<PredictedDemandPointDTO> predictedPoints) {
        this.predictedPoints = predictedPoints;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
