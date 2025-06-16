package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.CurrentStock;
import java.util.Optional;

public interface CurrentStockRepository {
    Optional<CurrentStock> findByProductId(Long productId);
    CurrentStock save(CurrentStock currentStock); // Handles both insert and update
    // int update(CurrentStock currentStock); // Could be separate
    // void insert(CurrentStock currentStock); // Could be separate
}
