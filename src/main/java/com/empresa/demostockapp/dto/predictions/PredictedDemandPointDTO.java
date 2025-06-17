package com.empresa.demostockapp.dto.predictions;

import java.time.LocalDate;

public class PredictedDemandPointDTO {

    private LocalDate date;
    private Double predictedValue;

    public PredictedDemandPointDTO() {
    }

    public PredictedDemandPointDTO(LocalDate date, Double predictedValue) {
        this.date = date;
        this.predictedValue = predictedValue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(Double predictedValue) {
        this.predictedValue = predictedValue;
    }
}
