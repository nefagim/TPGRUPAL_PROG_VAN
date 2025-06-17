package com.empresa.demostockapp.dto.export;

import com.empresa.demostockapp.model.SalesOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesDataExportDTO {

    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal sellingPrice;
    private LocalDateTime orderDate;

    public SalesDataExportDTO() {
    }

    public SalesDataExportDTO(SalesOrder salesOrder) {
        this.orderId = salesOrder.getId();
        if (salesOrder.getProduct() != null) {
            this.productId = salesOrder.getProduct().getId();
            this.productName = salesOrder.getProduct().getName();
        }
        this.quantitySold = salesOrder.getQuantitySold();
        this.sellingPrice = salesOrder.getSellingPrice();
        this.orderDate = salesOrder.getOrderDate();
    }

    // Getters
    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    // Setters
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantitySold(Integer quantitySold) {
        this.quantitySold = quantitySold;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
