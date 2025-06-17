package com.empresa.demostockapp.controller.predictions;

import com.empresa.demostockapp.dto.predictions.DemandPredictionRequestDTO;
import com.empresa.demostockapp.dto.predictions.DemandPredictionResponseDTO;
import com.empresa.demostockapp.service.predictions.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/demand")
    public ResponseEntity<DemandPredictionResponseDTO> getDemandPrediction(
            @Valid @RequestBody DemandPredictionRequestDTO requestDTO) {
        DemandPredictionResponseDTO responseDTO = predictionService.predictDemand(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
