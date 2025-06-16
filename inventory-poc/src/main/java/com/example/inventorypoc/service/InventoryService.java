package com.example.inventorypoc.service;

import com.example.inventorypoc.model.InventoryMovement;
import com.example.inventorypoc.model.CurrentStock;
import java.util.List;
import java.util.Optional;

public interface InventoryService {
    InventoryMovement recordMovement(InventoryMovement movement);
    Optional<CurrentStock> getCurrentStock(Long productId);
    List<InventoryMovement> getMovementsForProduct(Long productId);
}
