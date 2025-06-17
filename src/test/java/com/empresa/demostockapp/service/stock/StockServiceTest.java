package com.empresa.demostockapp.service.stock;

import com.empresa.demostockapp.dto.stock.StockItemResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.StockItem;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.StockItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService stockService;

    private Product product1;
    private StockItem stockItem1;

    @BeforeEach
    void setUp() {
        product1 = new Product("Product 1", "Desc 1", BigDecimal.valueOf(10.0), "SKU001");
        product1.setId(1L);

        stockItem1 = new StockItem(product1, 100);
        stockItem1.setId(1L);
        stockItem1.setLastUpdated(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getStockByProductId_found() {
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.of(stockItem1));

        StockItemResponseDTO response = stockService.getStockByProductId(1L);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getProductId());
        assertEquals(product1.getName(), response.getProductName());
        assertEquals(stockItem1.getQuantity(), response.getQuantity());
        assertEquals(stockItem1.getLastUpdated(), response.getLastUpdated());
        verify(stockItemRepository, times(1)).findByProductId(1L);
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void getStockByProductId_notFound_productExists_returnsZeroQuantity() {
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        StockItemResponseDTO response = stockService.getStockByProductId(1L);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getProductId());
        assertEquals(product1.getName(), response.getProductName());
        assertEquals(0, response.getQuantity());
        assertNull(response.getLastUpdated()); // No stock item, so lastUpdated should be null
        verify(stockItemRepository, times(1)).findByProductId(1L);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getStockByProductId_productNotFound_throwsException() {
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> stockService.getStockByProductId(1L));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(stockItemRepository, times(1)).findByProductId(1L);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void updateStockQuantity_createNewStockItem() {
        UpdateStockQuantityRequestDTO requestDTO = new UpdateStockQuantityRequestDTO(50);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.empty());

        // Mocking the save operation to return a StockItem with an ID and lastUpdated
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> {
            StockItem itemToSave = invocation.getArgument(0);
            itemToSave.setId(2L); // Simulate ID generation
            itemToSave.setLastUpdated(LocalDateTime.now()); // Simulate @PrePersist
            return itemToSave;
        });

        StockItemResponseDTO response = stockService.updateStockQuantity(1L, requestDTO);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getProductId());
        assertEquals(product1.getName(), response.getProductName());
        assertEquals(50, response.getQuantity());
        assertNotNull(response.getLastUpdated());
        verify(productRepository, times(1)).findById(1L);
        verify(stockItemRepository, times(1)).findByProductId(1L);
        verify(stockItemRepository, times(1)).save(any(StockItem.class));
    }

    @Test
    void updateStockQuantity_updateExistingStockItem() {
        UpdateStockQuantityRequestDTO requestDTO = new UpdateStockQuantityRequestDTO(75);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.of(stockItem1));

        // Mocking the save operation to update lastUpdated
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> {
            StockItem itemToSave = invocation.getArgument(0);
            itemToSave.setLastUpdated(LocalDateTime.now()); // Simulate @PreUpdate
            return itemToSave;
        });

        StockItemResponseDTO response = stockService.updateStockQuantity(1L, requestDTO);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getProductId());
        assertEquals(product1.getName(), response.getProductName());
        assertEquals(75, response.getQuantity());
        assertNotNull(response.getLastUpdated());
        // Assert that lastUpdated is newer than the original stockItem1.lastUpdated
        assertTrue(response.getLastUpdated().isAfter(stockItem1.getLastUpdated()));

        verify(productRepository, times(1)).findById(1L);
        verify(stockItemRepository, times(1)).findByProductId(1L);
        verify(stockItemRepository, times(1)).save(stockItem1); // verify that the existing item is saved
    }

    @Test
    void updateStockQuantity_productNotFound_throwsException() {
        UpdateStockQuantityRequestDTO requestDTO = new UpdateStockQuantityRequestDTO(50);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> stockService.updateStockQuantity(1L, requestDTO));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(stockItemRepository, never()).findByProductId(anyLong());
        verify(stockItemRepository, never()).save(any(StockItem.class));
    }
}
