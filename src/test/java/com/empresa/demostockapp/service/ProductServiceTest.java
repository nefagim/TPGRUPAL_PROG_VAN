package com.empresa.demostockapp.service;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        product = new Product("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123");
        product.setId(1L);

        productRequestDTO = new ProductRequestDTO("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123");
        productResponseDTO = new ProductResponseDTO(1L, "Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123");
    }

    @Test
    void createProduct_success() {
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getSku(), result.getSku());
        verify(productRepository, times(1)).findBySku(productRequestDTO.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_skuConflict() {
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(productRequestDTO));

        assertEquals("SKU already exists: " + productRequestDTO.getSku(), exception.getMessage());
        verify(productRepository, times(1)).findBySku(productRequestDTO.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_success() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        List<ProductResponseDTO> results = productService.getAllProducts();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(product.getName(), results.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_emptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductResponseDTO> results = productService.getAllProducts();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository, times(1)).findAll();
    }


    @Test
    void getProductById_success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1L));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void updateProduct_success() {
        ProductRequestDTO updatedRequestDTO = new ProductRequestDTO("Updated Name", "Updated Desc", BigDecimal.valueOf(120.00), "SKUNEW123");
        Product updatedProduct = new Product("Updated Name", "Updated Desc", BigDecimal.valueOf(120.00), "SKUNEW123");
        updatedProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySku("SKUNEW123")).thenReturn(Optional.empty()); // New SKU is unique
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponseDTO result = productService.updateProduct(1L, updatedRequestDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("SKUNEW123", result.getSku());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findBySku("SKUNEW123");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_success_sameSku() {
        ProductRequestDTO updatedRequestDTO = new ProductRequestDTO("Updated Name", "Updated Desc", BigDecimal.valueOf(120.00), "SKU123");
        Product updatedProduct = new Product("Updated Name", "Updated Desc", BigDecimal.valueOf(120.00), "SKU123");
        updatedProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        // findBySku should not be called for the same SKU if it's not being changed
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponseDTO result = productService.updateProduct(1L, updatedRequestDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("SKU123", result.getSku());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).findBySku(updatedRequestDTO.getSku()); // Not called if SKU doesn't change
        verify(productRepository, times(1)).save(any(Product.class));
    }


    @Test
    void updateProduct_notFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, productRequestDTO));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_skuConflict() {
        Product existingProductWithNewSku = new Product("Other Product", "Other Desc", BigDecimal.valueOf(50.00), "SKUNEW123");
        existingProductWithNewSku.setId(2L); // Different product ID

        ProductRequestDTO updatedRequestDTO = new ProductRequestDTO("Updated Name", "Updated Desc", BigDecimal.valueOf(120.00), "SKUNEW123");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySku("SKUNEW123")).thenReturn(Optional.of(existingProductWithNewSku));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(1L, updatedRequestDTO));

        assertEquals("New SKU already exists for another product: SKUNEW123", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findBySku("SKUNEW123");
        verify(productRepository, never()).save(any(Product.class));
    }


    @Test
    void deleteProduct_success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_notFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
