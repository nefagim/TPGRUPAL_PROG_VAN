package com.example.inventorypoc.service;

import com.example.inventorypoc.model.Product;
import com.example.inventorypoc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        // In a real app, add validation or other business logic here
        // For PoC, a new product must not have an ID or ID is 0.
        // The repository's save method needs to handle ID generation or be adapted.
        // For now, we assume the ID is either pre-set for update or handled by DB for insert.
        if (product.getId() != null && product.getId() != 0) {
            // This is more like an update, but create should be for new entities.
            // Throwing an exception or ensuring ID is null for creation is better.
            // For now, let's make it simple:
            // To align with typical create, let's clear ID if it's a "create" call.
             product.setId(null);
        }
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)); // Basic exception

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());

        productRepository.update(product); // Assuming update returns int (rows affected)
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
