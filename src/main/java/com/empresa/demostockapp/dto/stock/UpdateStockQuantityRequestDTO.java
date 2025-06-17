package com.empresa.demostockapp.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateStockQuantityRequestDTO {

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    public UpdateStockQuantityRequestDTO() {
    }

    public UpdateStockQuantityRequestDTO(Integer quantity) {
        this.quantity = quantity;
    }

    // Getter and Setter
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
