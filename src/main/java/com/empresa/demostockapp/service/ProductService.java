package com.empresa.demostockapp.service;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category; // Added
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.repository.CategoryRepository; // Added
import com.empresa.demostockapp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // Added

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) { // Updated constructor
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository; // Added
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        productRepository.findBySku(productRequestDTO.getSku()).ifPresent(p -> {
            throw new IllegalArgumentException("SKU already exists: " + productRequestDTO.getSku());
        });

        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        product.setSku(productRequestDTO.getSku());

        if (productRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequestDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product existingProduct = productRepository.findById(id) // Renamed product to existingProduct for clarity
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if SKU is being changed and if the new SKU already exists for another product
        if (!existingProduct.getSku().equals(productRequestDTO.getSku())) {
            productRepository.findBySku(productRequestDTO.getSku()).ifPresent(p -> { // Renamed existingProduct to p
                if (!p.getId().equals(id)) {
                    throw new IllegalArgumentException("New SKU already exists for another product: " + productRequestDTO.getSku());
                }
            });
        }

        existingProduct.setName(productRequestDTO.getName());
        existingProduct.setDescription(productRequestDTO.getDescription());
        existingProduct.setPrice(productRequestDTO.getPrice());
        existingProduct.setSku(productRequestDTO.getSku());

        if (productRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequestDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null); // Allow unsetting category
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private ProductResponseDTO convertToDTO(Product product) {
        // Uses the new constructor: public ProductResponseDTO(Product product)
        return new ProductResponseDTO(product);
    }
}
