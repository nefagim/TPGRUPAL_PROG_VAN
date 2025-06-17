package com.empresa.demostockapp.service.sales;

import com.empresa.demostockapp.dto.sales.SalesOrderRequestDTO;
import com.empresa.demostockapp.dto.sales.SalesOrderResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.exception.InsufficientStockException;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.model.StockItem;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.SalesOrderRepository;
import com.empresa.demostockapp.repository.StockItemRepository;
import com.empresa.demostockapp.service.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private StockItemRepository stockItemRepository;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private Product product1;
    private SalesOrderRequestDTO salesRequestDTO;
    private StockItem stockItemProduct1;

    @BeforeEach
    void setUp() {
        product1 = new Product("Product 1", "Desc 1", BigDecimal.valueOf(20.0), "SKU001");
        product1.setId(1L);

        salesRequestDTO = new SalesOrderRequestDTO(1L, 5, BigDecimal.valueOf(25.0));

        stockItemProduct1 = new StockItem(product1, 10); // Initial stock of 10
        stockItemProduct1.setId(1L);
    }

    @Test
    void recordSale_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.of(stockItemProduct1));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder so = invocation.getArgument(0);
            so.setId(1L);
            so.setOrderDate(LocalDateTime.now()); // Simulate @PrePersist
            return so;
        });

        SalesOrderResponseDTO response = salesOrderService.recordSale(salesRequestDTO);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getProductId());
        assertEquals(product1.getName(), response.getProductName());
        assertEquals(salesRequestDTO.getQuantitySold(), response.getQuantitySold());
        assertEquals(salesRequestDTO.getSellingPrice(), response.getSellingPrice());
        assertNotNull(response.getOrderDate());

        ArgumentCaptor<UpdateStockQuantityRequestDTO> stockUpdateCaptor = ArgumentCaptor.forClass(UpdateStockQuantityRequestDTO.class);
        verify(stockService, times(1)).updateStockQuantity(eq(1L), stockUpdateCaptor.capture());
        assertEquals(5, stockUpdateCaptor.getValue().getQuantity()); // 10 (initial) - 5 (sold) = 5

        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }

    @Test
    void recordSale_success_noInitialStockRecord() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.empty()); // No stock item initially

        // This should throw InsufficientStockException because initial stock is considered 0
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> salesOrderService.recordSale(salesRequestDTO));

        assertTrue(exception.getMessage().contains("Insufficient stock for product: Product 1"));

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(stockService, never()).updateStockQuantity(anyLong(), any());
    }


    @Test
    void recordSale_productNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.recordSale(salesRequestDTO));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(stockService, never()).updateStockQuantity(anyLong(), any(UpdateStockQuantityRequestDTO.class));
    }

    @Test
    void recordSale_insufficientStock() {
        stockItemProduct1.setQuantity(3); // Only 3 in stock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(stockItemRepository.findByProductId(1L)).thenReturn(Optional.of(stockItemProduct1));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> salesOrderService.recordSale(salesRequestDTO)); // Requesting 5

        assertEquals("Insufficient stock for product: Product 1. Available: 3, Requested: 5", exception.getMessage());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(stockService, never()).updateStockQuantity(anyLong(), any(UpdateStockQuantityRequestDTO.class));
    }


    @Test
    void getSalesForProduct_success_withSales() {
        SalesOrder so1 = new SalesOrder(product1, 2, BigDecimal.valueOf(25.0));
        so1.setId(1L); so1.setOrderDate(LocalDateTime.now());
        SalesOrder so2 = new SalesOrder(product1, 3, BigDecimal.valueOf(24.0));
        so2.setId(2L); so2.setOrderDate(LocalDateTime.now());

        when(productRepository.existsById(1L)).thenReturn(true);
        when(salesOrderRepository.findByProductId(1L)).thenReturn(List.of(so1, so2));

        List<SalesOrderResponseDTO> responses = salesOrderService.getSalesForProduct(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(product1.getName(), responses.get(0).getProductName());
        verify(salesOrderRepository, times(1)).findByProductId(1L);
    }

    @Test
    void getSalesForProduct_success_noSales() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(salesOrderRepository.findByProductId(1L)).thenReturn(Collections.emptyList());

        List<SalesOrderResponseDTO> responses = salesOrderService.getSalesForProduct(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(salesOrderRepository, times(1)).findByProductId(1L);
    }

    @Test
    void getSalesForProduct_productNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.getSalesForProduct(1L));

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(salesOrderRepository, never()).findByProductId(anyLong());
    }
}
