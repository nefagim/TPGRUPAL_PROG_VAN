package com.example.inventorypoc.service;

import com.example.inventorypoc.model.CurrentStock;
import com.example.inventorypoc.model.InventoryMovement;
import com.example.inventorypoc.model.Product;
import com.example.inventorypoc.repository.CurrentStockRepository;
import com.example.inventorypoc.repository.InventoryMovementRepository;
import com.example.inventorypoc.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private CurrentStockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product1;
    private InventoryMovement movementIn;
    private InventoryMovement movementOut;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Test Product 1", "Description 1", "Category 1", 10.0);
        movementIn = new InventoryMovement(null, 1L, InventoryMovement.MovementType.IN, 10, LocalDateTime.now(), "Stock In");
        movementOut = new InventoryMovement(null, 1L, InventoryMovement.MovementType.OUT, 5, LocalDateTime.now(), "Stock Out");
    }

    @Test
    void recordMovement_inbound_newStock_shouldSaveMovementAndUpdateStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(movementIn); // Assume ID gets set by repo
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty()); // No existing stock
        when(stockRepository.save(any(CurrentStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovement result = inventoryService.recordMovement(movementIn);

        assertNotNull(result);
        assertEquals(10, result.getQuantity());
        verify(stockRepository).save(argThat(stock -> stock.getProductId().equals(1L) && stock.getQuantity() == 10));
    }

    @Test
    void recordMovement_inbound_existingStock_shouldSaveMovementAndUpdateStock() {
        CurrentStock existingStock = new CurrentStock(1L, 5, LocalDateTime.now().minusDays(1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(movementIn);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(CurrentStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovement result = inventoryService.recordMovement(movementIn);

        assertNotNull(result);
        verify(stockRepository).save(argThat(stock -> stock.getProductId().equals(1L) && stock.getQuantity() == 15)); // 5 + 10
    }

    @Test
    void recordMovement_outbound_sufficientStock_shouldSaveMovementAndUpdateStock() {
        CurrentStock existingStock = new CurrentStock(1L, 10, LocalDateTime.now().minusDays(1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(movementOut);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(CurrentStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovement result = inventoryService.recordMovement(movementOut);

        assertNotNull(result);
        verify(stockRepository).save(argThat(stock -> stock.getProductId().equals(1L) && stock.getQuantity() == 5)); // 10 - 5
    }

    @Test
    void recordMovement_outbound_insufficientStock_shouldThrowException() {
        CurrentStock existingStock = new CurrentStock(1L, 3, LocalDateTime.now().minusDays(1)); // Only 3 in stock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        // MovementRepository.save will not be called if stock is insufficient before that
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(existingStock));
        // stockRepository.save will not be called either

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.recordMovement(movementOut); // Attempting to take 5
        });

        assertEquals("Insufficient stock for product id: 1", exception.getMessage());
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(stockRepository, never()).save(any(CurrentStock.class));
    }

    @Test
    void recordMovement_productNotFound_shouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        InventoryMovement movementForNonExistingProduct = new InventoryMovement(null, 99L, InventoryMovement.MovementType.IN, 10, LocalDateTime.now(), "Test");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.recordMovement(movementForNonExistingProduct);
        });

        assertEquals("Product not found with id: 99", exception.getMessage());
        verify(movementRepository, never()).save(any());
        verify(stockRepository, never()).findByProductId(anyLong());
        verify(stockRepository, never()).save(any());
    }


    @Test
    void getCurrentStock_shouldReturnStockFromRepository() {
        CurrentStock stock = new CurrentStock(1L, 20, LocalDateTime.now());
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        Optional<CurrentStock> foundStock = inventoryService.getCurrentStock(1L);

        assertTrue(foundStock.isPresent());
        assertEquals(20, foundStock.get().getQuantity());
        verify(stockRepository, times(1)).findByProductId(1L);
    }

    @Test
    void getMovementsForProduct_shouldReturnMovementsFromRepository() {
        List<InventoryMovement> movements = Arrays.asList(movementIn, movementOut);
        when(movementRepository.findByProductId(1L)).thenReturn(movements);

        List<InventoryMovement> foundMovements = inventoryService.getMovementsForProduct(1L);

        assertEquals(2, foundMovements.size());
        verify(movementRepository, times(1)).findByProductId(1L);
    }
}
