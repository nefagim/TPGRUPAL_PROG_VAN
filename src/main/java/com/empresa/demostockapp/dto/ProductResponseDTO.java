package com.empresa.demostockapp.dto;

import com.empresa.demostockapp.dto.category.CategoryResponseDTO; // Added import
import com.empresa.demostockapp.model.Product; // Added import
import java.math.BigDecimal;

public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private CategoryResponseDTO category; // Added category field

    public ProductResponseDTO() {
    }

    // Existing constructor - can be kept or removed if the new one is preferred
    public ProductResponseDTO(Long id, String name, String description, BigDecimal price, String sku, CategoryResponseDTO category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sku = sku;
        this.category = category;
    }

    // New constructor from Product entity
    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.sku = product.getSku();
        if (product.getCategory() != null) {
            this.category = CategoryResponseDTO.fromCategory(product.getCategory());
        } else {
            this.category = null;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public CategoryResponseDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDTO category) {
        this.category = category;
    }
}
