package com.example.inventorypoc.controller;

import com.example.inventorypoc.model.CurrentStock;
import com.example.inventorypoc.model.InventoryMovement;
import com.example.inventorypoc.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/movements")
    public ResponseEntity<?> recordMovement(@RequestBody InventoryMovement movement) {
        try {
            InventoryMovement recordedMovement = inventoryService.recordMovement(movement);
            return new ResponseEntity<>(recordedMovement, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Catching specific exceptions and returning appropriate error codes would be better
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<CurrentStock> getCurrentStock(@PathVariable Long productId) {
        Optional<CurrentStock> stock = inventoryService.getCurrentStock(productId);
        return stock.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/movements/{productId}")
    public ResponseEntity<List<InventoryMovement>> getMovementsForProduct(@PathVariable Long productId) {
        List<InventoryMovement> movements = inventoryService.getMovementsForProduct(productId);
        return ResponseEntity.ok(movements);
    }
}
