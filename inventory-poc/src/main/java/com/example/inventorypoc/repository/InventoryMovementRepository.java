package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.InventoryMovement;
import java.util.List;

public interface InventoryMovementRepository {
    InventoryMovement save(InventoryMovement movement);
    List<InventoryMovement> findByProductId(Long productId);
    // Potentially other methods like findByDateRange, etc.
}
