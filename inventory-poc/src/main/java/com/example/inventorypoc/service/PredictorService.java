package com.example.inventorypoc.service;

import com.example.inventorypoc.model.CurrentStock; // Assuming we might return a similar structure or just a value
import java.util.Optional;

public interface PredictorService {
    // For PoC, predicted stock could be a simple DTO or just the quantity
    // Let's define a simple DTO for the prediction
    class PredictedStockInfo {
        private Long productId;
        private int currentQuantity;
        private int predictedQuantity;
        private String predictionBasis; // e.g., "Current stock + 10%"

        public PredictedStockInfo(Long productId, int currentQuantity, int predictedQuantity, String predictionBasis) {
            this.productId = productId;
            this.currentQuantity = currentQuantity;
            this.predictedQuantity = predictedQuantity;
            this.predictionBasis = predictionBasis;
        }
        // Getters
        public Long getProductId() { return productId; }
        public int getCurrentQuantity() { return currentQuantity; }
        public int getPredictedQuantity() { return predictedQuantity; }
        public String getPredictionBasis() { return predictionBasis; }
    }

    Optional<PredictedStockInfo> getPredictedStock(Long productId);
}
