package com.example.inventorypoc.model;

import java.time.LocalDateTime;

public class InventoryMovement {
    private Long id;
    private Long productId;
    private MovementType type; // e.g., IN, OUT
    private int quantity;
    private LocalDateTime timestamp;
    private String notes; // Optional notes like source, reason for adjustment etc.

    public enum MovementType {
        IN, // Stock received, purchase, positive adjustment
        OUT // Stock sold, dispatched, negative adjustment, spoilage
    }

    // Constructors
    public InventoryMovement() {
        this.timestamp = LocalDateTime.now();
    }

    public InventoryMovement(Long id, Long productId, MovementType type, int quantity, LocalDateTime timestamp, String notes) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "InventoryMovement{" +
                "id=" + id +
                ", productId=" + productId +
                ", type=" + type +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", notes='" + notes + '\'' +
                '}';
    }
}
