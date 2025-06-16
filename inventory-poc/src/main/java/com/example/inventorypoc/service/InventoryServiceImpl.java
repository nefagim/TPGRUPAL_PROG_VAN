package com.example.inventorypoc.service;

import com.example.inventorypoc.model.CurrentStock;
import com.example.inventorypoc.model.InventoryMovement;
import com.example.inventorypoc.repository.CurrentStockRepository;
import com.example.inventorypoc.repository.InventoryMovementRepository;
import com.example.inventorypoc.repository.ProductRepository; // To check if product exists
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final CurrentStockRepository stockRepository;
    private final ProductRepository productRepository; // To validate product existence

    @Autowired
    public InventoryServiceImpl(InventoryMovementRepository movementRepository,
                                CurrentStockRepository stockRepository,
                                ProductRepository productRepository) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional // Ensures atomicity for saving movement and updating stock
    public InventoryMovement recordMovement(InventoryMovement movement) {
        // Validate product exists
        productRepository.findById(movement.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + movement.getProductId()));

        // Save the movement
        InventoryMovement savedMovement = movementRepository.save(movement);

        // Update current stock
        CurrentStock stock = stockRepository.findByProductId(movement.getProductId())
                .orElse(new CurrentStock(movement.getProductId(), 0));

        if (movement.getType() == InventoryMovement.MovementType.IN) {
            stock.setQuantity(stock.getQuantity() + movement.getQuantity());
        } else if (movement.getType() == InventoryMovement.MovementType.OUT) {
            int newQuantity = stock.getQuantity() - movement.getQuantity();
            if (newQuantity < 0) {
                // Handle insufficient stock - for PoC, throw exception
                throw new RuntimeException("Insufficient stock for product id: " + movement.getProductId());
            }
            stock.setQuantity(newQuantity);
        }
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        return savedMovement;
    }

    @Override
    public Optional<CurrentStock> getCurrentStock(Long productId) {
        return stockRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryMovement> getMovementsForProduct(Long productId) {
        return movementRepository.findByProductId(productId);
    }
}
