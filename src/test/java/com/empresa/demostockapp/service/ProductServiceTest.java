package com.empresa.demostockapp.service;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.repository.CategoryRepository;
import com.empresa.demostockapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Mock
    private CategoryRepository categoryRepository; // Added

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Product productWithCategory;
    private Category category;
    private ProductRequestDTO productRequestDTO; // Base DTO without categoryId for some tests

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(10L);

        product = new Product("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123", null);
        product.setId(1L);

        productWithCategory = new Product("Product With Cat", "Desc With Cat", BigDecimal.valueOf(150.00), "SKU456", category);
        productWithCategory.setId(2L);

        // Base request DTO, categoryId can be set per test
        productRequestDTO = new ProductRequestDTO("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123", null);
    }

    // --- CreateProduct Tests ---
    @Test
    void createProduct_success_withoutCategory() {
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product); // product has null category

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertNull(result.getCategory()); // Verify category is null in response
        verify(productRepository).save(argThat(p -> p.getCategory() == null));
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void createProduct_success_withCategory() {
        productRequestDTO.setCategoryId(10L);
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        // Capture the product passed to save
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        // Mock save to return the captured product with an ID, simulating save operation
        when(productRepository.save(productArgumentCaptor.capture())).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(3L); // Simulate ID generation
            return p;
        });

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(productRequestDTO.getName(), result.getName());
        assertNotNull(result.getCategory());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(category.getName(), result.getCategory().getName());

        Product capturedProduct = productArgumentCaptor.getValue();
        assertNotNull(capturedProduct.getCategory());
        assertEquals(category.getId(), capturedProduct.getCategory().getId());
    }

    @Test
    void createProduct_invalidCategoryId_throwsResourceNotFound() {
        productRequestDTO.setCategoryId(99L); // Invalid category ID
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productRequestDTO));
        assertEquals("Category not found with id: 99", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_skuConflict() {
        // This test remains largely the same, category handling is not reached if SKU conflicts.
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(product));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(productRequestDTO));
        assertEquals("SKU already exists: " + productRequestDTO.getSku(), exception.getMessage());
    }

    // --- GetProduct Tests ---
    @Test
    void getProductById_success_withCategory() {
        when(productRepository.findById(2L)).thenReturn(Optional.of(productWithCategory));
        ProductResponseDTO result = productService.getProductById(2L);
        assertNotNull(result);
        assertEquals(productWithCategory.getName(), result.getName());
        assertNotNull(result.getCategory());
        assertEquals(category.getName(), result.getCategory().getName());
    }

    @Test
    void getProductById_success_withoutCategory() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // product has no category
        ProductResponseDTO result = productService.getProductById(1L);
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertNull(result.getCategory());
    }

    // --- UpdateProduct Tests ---
    @Test
    void updateProduct_success_withCategoryChange() {
        ProductRequestDTO updateRequest = new ProductRequestDTO("Updated", "Desc", BigDecimal.ONE, "SKU123", 10L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // product initially has no category
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO result = productService.updateProduct(1L, updateRequest);
        assertNotNull(result.getCategory());
        assertEquals(category.getId(), result.getCategory().getId());
        verify(productRepository).save(argThat(p -> p.getCategory() != null && p.getCategory().getId().equals(10L)));
    }

    @Test
    void updateProduct_success_setCategoryToNull() {
        ProductRequestDTO updateRequest = new ProductRequestDTO("Updated", "Desc", BigDecimal.ONE, "SKU456", null);
        // productWithCategory initially has a category
        when(productRepository.findById(2L)).thenReturn(Optional.of(productWithCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO result = productService.updateProduct(2L, updateRequest);
        assertNull(result.getCategory());
        verify(productRepository).save(argThat(p -> p.getCategory() == null));
        verify(categoryRepository, never()).findById(anyLong()); // No lookup if categoryId is null
    }

    @Test
    void updateProduct_invalidCategoryId_throwsResourceNotFound() {
        ProductRequestDTO updateRequest = new ProductRequestDTO("Updated", "Desc", BigDecimal.ONE, "SKU123", 99L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, updateRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    // --- Existing tests that should still pass or need minor review ---
    @Test
    void getAllProducts_success() { // If products have categories, they should be in DTO
        product.setCategory(category); // Add category to product for this test
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
        List<ProductResponseDTO> results = productService.getAllProducts();
        assertNotNull(results);
        assertEquals(1, results.size());
        assertNotNull(results.get(0).getCategory());
        assertEquals(category.getName(), results.get(0).getCategory().getName());
    }

    @Test
    void getProductById_notFound() { // No changes needed
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void updateProduct_notFound() { // No changes needed if product itself not found
        ProductRequestDTO updatedRequestDTO = new ProductRequestDTO("Name", "Desc", BigDecimal.ONE, "SKU", null);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, updatedRequestDTO));
    }

    @Test
    void deleteProduct_success() { // No changes needed
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).delete(product);
    }
}
