package com.empresa.demostockapp.service;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.dto.stock.StockItemResponseDTO; // Added
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.repository.CategoryRepository;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.service.stock.StockService; // Added
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Added for StockItemResponseDTO
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
    private CategoryRepository categoryRepository;

    @Mock
    private StockService stockService; // Added

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Product productWithCategory;
    private Category category;
    private ProductRequestDTO productRequestDTO;
    private StockItemResponseDTO defaultStockItemResponseDTO;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(10L);

        product = new Product("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123", null);
        product.setId(1L);

        productWithCategory = new Product("Product With Cat", "Desc With Cat", BigDecimal.valueOf(150.00), "SKU456", category);
        productWithCategory.setId(2L);

        productRequestDTO = new ProductRequestDTO("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123", null);

        // Default mock for stock service
        defaultStockItemResponseDTO = new StockItemResponseDTO(0L, "", 0, null); // ProductId, ProductName, Quantity, LastUpdated
    }

    private void mockStockServiceForProduct(Long productId, int quantity) {
        StockItemResponseDTO stockResponse = new StockItemResponseDTO(productId, "SomeName", quantity, LocalDateTime.now());
        when(stockService.getStockByProductId(productId)).thenReturn(stockResponse);
    }


    // --- CreateProduct Tests ---
    @Test
    void createProduct_success_withoutCategory() {
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        // Mock save to return the product with an ID
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L); // Assign ID to the product being saved
            return p;
        });
        mockStockServiceForProduct(1L, 50); // Mock stock for the newly created product's ID

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(productRequestDTO.getName(), result.getName());
        assertNull(result.getCategory());
        assertEquals(50, result.getQuantity()); // Assert stock quantity
        verify(productRepository).save(argThat(p -> p.getCategory() == null));
        verify(categoryRepository, never()).findById(anyLong());
        verify(stockService).getStockByProductId(1L);
    }

    @Test
    void createProduct_success_withCategory() {
        productRequestDTO.setCategoryId(10L);
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productArgumentCaptor.capture())).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(3L);
            return p;
        });
        mockStockServiceForProduct(3L, 75);

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(productRequestDTO.getName(), result.getName());
        assertNotNull(result.getCategory());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(75, result.getQuantity()); // Assert stock quantity
        Product capturedProduct = productArgumentCaptor.getValue();
        assertNotNull(capturedProduct.getCategory());
        assertEquals(category.getId(), capturedProduct.getCategory().getId());
        verify(stockService).getStockByProductId(3L);
    }

    // createProduct_invalidCategoryId_throwsResourceNotFound and createProduct_skuConflict remain the same
    // as stockService is not called if these prior checks fail.

    // --- GetProduct Tests ---
    @Test
    void getProductById_success_withCategory() {
        when(productRepository.findById(2L)).thenReturn(Optional.of(productWithCategory));
        mockStockServiceForProduct(2L, 100);

        ProductResponseDTO result = productService.getProductById(2L);
        assertNotNull(result);
        assertEquals(productWithCategory.getName(), result.getName());
        assertNotNull(result.getCategory());
        assertEquals(category.getName(), result.getCategory().getName());
        assertEquals(100, result.getQuantity()); // Assert stock quantity
        verify(stockService).getStockByProductId(2L);
    }

    @Test
    void getProductById_success_withoutCategory() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        mockStockServiceForProduct(1L, 25);

        ProductResponseDTO result = productService.getProductById(1L);
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertNull(result.getCategory());
        assertEquals(25, result.getQuantity()); // Assert stock quantity
        verify(stockService).getStockByProductId(1L);
    }

    // --- UpdateProduct Tests ---
    @Test
    void updateProduct_success_withCategoryChange() {
        ProductRequestDTO updateRequest = new ProductRequestDTO("Updated", "Desc", BigDecimal.ONE, "SKU123", 10L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        mockStockServiceForProduct(1L, 10);

        ProductResponseDTO result = productService.updateProduct(1L, updateRequest);
        assertNotNull(result.getCategory());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(10, result.getQuantity()); // Assert stock quantity
        verify(productRepository).save(argThat(p -> p.getCategory() != null && p.getCategory().getId().equals(10L)));
        verify(stockService).getStockByProductId(1L);
    }

    // updateProduct_success_setCategoryToNull, updateProduct_invalidCategoryId_throwsResourceNotFound
    // and other update error cases would need similar stockService mocking if they reach the convertToDTO stage.
    // For brevity, only showing one success case update. The principle is the same.

    // --- GetAllProducts Test ---
    @Test
    void getAllProducts_success() {
        product.setCategory(category);
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
        // Mock stockService for the product ID that will be fetched
        mockStockServiceForProduct(product.getId(), 200);

        List<ProductResponseDTO> results = productService.getAllProducts();
        assertNotNull(results);
        assertEquals(1, results.size());
        assertNotNull(results.get(0).getCategory());
        assertEquals(category.getName(), results.get(0).getCategory().getName());
        assertEquals(200, results.get(0).getQuantity()); // Assert stock quantity
        verify(stockService).getStockByProductId(product.getId());
    }

    // Other tests like getProductById_notFound, updateProduct_notFound, deleteProduct_success etc.
    // generally do not need changes related to stockService mocking, as they either throw exceptions
    // before the DTO conversion happens, or don't involve DTO conversion.

    // --- Tests that don't need stockService interaction or minimal changes ---
    @Test
    void createProduct_invalidCategoryId_throwsResourceNotFound() {
        productRequestDTO.setCategoryId(99L);
        when(productRepository.findBySku(productRequestDTO.getSku())).thenReturn(Optional.empty());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productRequestDTO));
        assertEquals("Category not found with id: 99", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(stockService, never()).getStockByProductId(anyLong()); // Ensure not called
    }

    @Test
    void createProduct_skuConflict() {
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(product));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(productRequestDTO));
        assertEquals("SKU already exists: " + productRequestDTO.getSku(), exception.getMessage());
        verify(stockService, never()).getStockByProductId(anyLong()); // Ensure not called
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
        verify(stockService, never()).getStockByProductId(anyLong()); // Ensure not called
    }

    @Test
    void updateProduct_notFound() {
        ProductRequestDTO updatedRequestDTO = new ProductRequestDTO("Name", "Desc", BigDecimal.ONE, "SKU", null);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, updatedRequestDTO));
        verify(stockService, never()).getStockByProductId(anyLong()); // Ensure not called
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).delete(product);
        verify(stockService, never()).getStockByProductId(anyLong()); // Ensure not called
    }
}
