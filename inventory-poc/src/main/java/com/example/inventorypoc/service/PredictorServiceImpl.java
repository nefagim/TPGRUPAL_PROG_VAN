package com.example.inventorypoc.service;

import com.example.inventorypoc.model.CurrentStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PredictorServiceImpl implements PredictorService {

    private final InventoryService inventoryService; // Use existing service to get current stock

    @Autowired
    public PredictorServiceImpl(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public Optional<PredictedStockInfo> getPredictedStock(Long productId) {
        Optional<CurrentStock> currentStockOpt = inventoryService.getCurrentStock(productId);

        if (currentStockOpt.isPresent()) {
            CurrentStock currentStock = currentStockOpt.get();
            // Basic placeholder logic: predict 10% more than current stock
            int predictedQuantity = (int) (currentStock.getQuantity() * 1.1);
            String basis = "Current stock (" + currentStock.getQuantity() + ") + 10%";

            PredictedStockInfo prediction = new PredictedStockInfo(
                productId,
                currentStock.getQuantity(),
                predictedQuantity,
                basis
            );
            return Optional.of(prediction);
        } else {
            return Optional.empty(); // No current stock, so no prediction
        }
    }
}
