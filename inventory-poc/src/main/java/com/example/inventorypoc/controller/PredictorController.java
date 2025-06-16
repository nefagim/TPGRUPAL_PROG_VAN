package com.example.inventorypoc.controller;

import com.example.inventorypoc.service.PredictorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/predictor")
public class PredictorController {

    private final PredictorService predictorService;

    @Autowired
    public PredictorController(PredictorService predictorService) {
        this.predictorService = predictorService;
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<PredictorService.PredictedStockInfo> getPredictedStock(@PathVariable Long productId) {
        Optional<PredictorService.PredictedStockInfo> prediction = predictorService.getPredictedStock(productId);
        return prediction.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
