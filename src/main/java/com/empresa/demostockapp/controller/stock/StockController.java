package com.empresa.demostockapp.controller.stock;

import com.empresa.demostockapp.dto.stock.StockItemResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.service.stock.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<StockItemResponseDTO> getStockByProductId(@PathVariable Long productId) {
        StockItemResponseDTO responseDTO = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<StockItemResponseDTO> updateStockQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockQuantityRequestDTO requestDTO) {
        StockItemResponseDTO responseDTO = stockService.updateStockQuantity(productId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
