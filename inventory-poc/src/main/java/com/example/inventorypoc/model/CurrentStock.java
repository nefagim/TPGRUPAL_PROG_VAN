package com.example.inventorypoc.model;

import java.time.LocalDateTime;

public class CurrentStock {
    private Long productId; // Primary key, also foreign key to Product
    private int quantity;
    private LocalDateTime lastUpdated;

    // Constructors
    public CurrentStock() {
        this.lastUpdated = LocalDateTime.now();
    }

    public CurrentStock(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    public CurrentStock(Long productId, int quantity, LocalDateTime lastUpdated) {
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "CurrentStock{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
