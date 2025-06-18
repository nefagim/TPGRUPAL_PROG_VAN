package com.empresa.demostockapp.service;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category; // Added
import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.dto.stock.StockItemResponseDTO; // Added
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.repository.CategoryRepository;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.service.stock.StockService; // Added
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService; // Added

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          StockService stockService) { // Updated constructor
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockService = stockService; // Added
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
        // Uses the constructor: public ProductResponseDTO(Product product)
        // then sets quantity separately
        ProductResponseDTO dto = new ProductResponseDTO(product);

        // Fetch and set stock quantity
        // Note: This introduces an N+1 potential in getAllProducts if not handled carefully.
        // For this iteration, we accept it. Future optimization could batch these calls.
        StockItemResponseDTO stockInfo = stockService.getStockByProductId(product.getId());
        dto.setQuantity(stockInfo.getQuantity());

        return dto;
    }
}
