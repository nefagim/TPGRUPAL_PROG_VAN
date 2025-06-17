package com.empresa.demostockapp.dto.stock;

import java.time.LocalDateTime;

public class StockItemResponseDTO {

    private Long productId;
    private String productName;
    private Integer quantity;
    private LocalDateTime lastUpdated;

    public StockItemResponseDTO() {
    }

    public StockItemResponseDTO(Long productId, String productName, Integer quantity, LocalDateTime lastUpdated) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
