package com.empresa.demostockapp.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesOrderResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal sellingPrice;
    private LocalDateTime orderDate;

    public SalesOrderResponseDTO() {
    }

    public SalesOrderResponseDTO(Long id, Long productId, String productName, Integer quantitySold, BigDecimal sellingPrice, LocalDateTime orderDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.sellingPrice = sellingPrice;
        this.orderDate = orderDate;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
