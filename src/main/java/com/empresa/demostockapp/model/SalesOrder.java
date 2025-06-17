package com.empresa.demostockapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY is often a good default for ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @Column(nullable = false)
    @NotNull
    @Positive(message = "Quantity sold must be positive")
    private Integer quantitySold;

    @Column(nullable = false)
    @NotNull
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    public SalesOrder() {
    }

    public SalesOrder(Product product, Integer quantitySold, BigDecimal sellingPrice) {
        this.product = product;
        this.quantitySold = quantitySold;
        this.sellingPrice = sellingPrice;
    }

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
