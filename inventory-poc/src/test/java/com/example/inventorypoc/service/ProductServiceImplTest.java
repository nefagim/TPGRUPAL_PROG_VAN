package com.example.inventorypoc.service;

import com.example.inventorypoc.model.Product;
import com.example.inventorypoc.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product productToCreate;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Test Product 1", "Description 1", "Category 1", 10.0);
        productToCreate = new Product(null, "New Product", "New Desc", "New Cat", 5.0);
    }

    @Test
    void createProduct_shouldSaveAndReturnProduct() {
        // Ensure ID is null before creation, service should handle this or repo handles generation
        Product productForSave = new Product(null, productToCreate.getName(), productToCreate.getDescription(), productToCreate.getCategory(), productToCreate.getPrice());

        // Mock repository save to return product with an ID
        Product savedProduct = new Product(1L, productToCreate.getName(), productToCreate.getDescription(), productToCreate.getCategory(), productToCreate.getPrice());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product created = productService.createProduct(productForSave);

        assertNotNull(created);
        assertNotNull(created.getId()); // ID should be set by save (mocked here)
        assertEquals("New Product", created.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_withNonNullId_shouldSetIdToNullAndSave() {
        Product productWithId = new Product(5L, "Product With ID", "Desc", "Cat", 25.0);
        Product savedProduct = new Product(1L, "Product With ID", "Desc", "Cat", 25.0); // Simulating new ID generation

        // Argument captor to check the product passed to repository
        org.mockito.ArgumentCaptor<Product> productArgumentCaptor = org.mockito.ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productArgumentCaptor.capture())).thenReturn(savedProduct);

        Product created = productService.createProduct(productWithId);

        assertNotNull(created);
        assertEquals(1L, created.getId()); // Should have the new ID
        assertNull(productArgumentCaptor.getValue().getId()); // Verify ID was set to null before save
        verify(productRepository, times(1)).save(any(Product.class));
    }


    @Test
    void getAllProducts_shouldReturnAllProducts() {
        Product product2 = new Product(2L, "Test Product 2", "Description 2", "Category 2", 20.0);
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> found = productService.getAllProducts();

        assertEquals(2, found.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        Optional<Product> found = productService.getProductById(1L);

        assertTrue(found.isPresent());
        assertEquals(product1.getName(), found.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_whenProductDoesNotExist_shouldReturnEmpty() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Product> found = productService.getProductById(3L);

        assertFalse(found.isPresent());
        verify(productRepository, times(1)).findById(3L);
    }

    @Test
    void updateProduct_whenProductExists_shouldUpdateAndReturnProduct() {
        Product productDetailsToUpdate = new Product(null, "Updated Name", "Updated Desc", "Updated Cat", 15.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1)); // Return existing product
        // The actual update in JdbcProductRepository returns int, not the product.
        // The service method re-constructs or uses the passed product.
        // Let's assume the update operation in repo is successful
        when(productRepository.update(any(Product.class))).thenReturn(1);


        Product updated = productService.updateProduct(1L, productDetailsToUpdate);

        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
        assertEquals(1L, updated.getId()); // ID should remain the same

        // Verify findById was called, and then update was called on the repository
        verify(productRepository, times(1)).findById(1L); // Corrected: removed extra parenthesis
        verify(productRepository, times(1)).update(any(Product.class));
    }

    @Test
    void updateProduct_whenProductDoesNotExist_shouldThrowException() {
        Product productDetailsToUpdate = new Product(null, "Updated Name", "Updated Desc", "Updated Cat", 15.0);
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(3L, productDetailsToUpdate);
        });
        assertEquals("Product not found with id: 3", exception.getMessage());
        verify(productRepository, times(1)).findById(3L));
        verify(productRepository, never()).save(any(Product.class)); // Or update // Corrected: removed extra parenthesis
    }

    @Test
    void deleteProduct_shouldCallRepositoryDelete() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}
