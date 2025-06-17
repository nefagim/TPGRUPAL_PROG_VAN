package com.empresa.demostockapp.dto.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class SalesOrderRequestDTO {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity sold cannot be null")
    @Positive(message = "Quantity sold must be positive")
    private Integer quantitySold;

    @NotNull(message = "Selling price cannot be null")
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    public SalesOrderRequestDTO() {
    }

    public SalesOrderRequestDTO(Long productId, Integer quantitySold, BigDecimal sellingPrice) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.sellingPrice = sellingPrice;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Integer quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}
