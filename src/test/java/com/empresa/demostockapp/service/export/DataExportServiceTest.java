package com.empresa.demostockapp.service.export;

import com.empresa.demostockapp.dto.export.SalesDataExportDTO;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.model.Category; // Added import
import com.empresa.demostockapp.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private DataExportService dataExportService;

    private Product product1;
    private Product product2; // Added for no-category testing
    private Category category1; // Added
    private SalesOrder salesOrder1, salesOrder2;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;


    @BeforeEach
    void setUp() {
        category1 = new Category("Electronics", "Electronic devices");
        category1.setId(10L);

        product1 = new Product("Test Product", "Description", BigDecimal.TEN, "SKU001", category1); // Assign category
        product1.setId(1L);

        product2 = new Product("Another Product", "Description", BigDecimal.ONE, "SKU002", null); // No category
        product2.setId(2L);

        salesOrder1 = new SalesOrder(product1, 5, BigDecimal.valueOf(50));
        salesOrder1.setId(101L);
        salesOrder1.setOrderDate(LocalDateTime.now().minusDays(1));

        salesOrder2 = new SalesOrder(product2, 10, BigDecimal.valueOf(10));
        salesOrder2.setId(102L);
        salesOrder2.setOrderDate(LocalDateTime.now().minusDays(2));

        startDate = LocalDate.now().minusDays(10);
        endDate = LocalDate.now();
        startDateTime = startDate.atStartOfDay();
        endDateTime = endDate.atTime(LocalTime.MAX);
    }

    @Test
    void getSalesDataForExport_withProductId_success() {
        // product1 has category1 ("Electronics")
        when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(salesOrder1));

        List<SalesDataExportDTO> result = dataExportService.getSalesDataForExport(startDate, endDate, Optional.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        SalesDataExportDTO dto = result.get(0);
        assertEquals(salesOrder1.getId(), dto.getOrderId());
        assertEquals(product1.getId(), dto.getProductId());
        assertEquals(product1.getName(), dto.getProductName());
        assertEquals(salesOrder1.getQuantitySold(), dto.getQuantitySold());
        assertEquals(category1.getName(), dto.getCategoryName()); // Assert category name

        verify(salesOrderRepository).findByProductIdAndOrderDateBetween(eq(1L), eq(startDateTime), eq(endDateTime));
        verify(salesOrderRepository, never()).findAllByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getSalesDataForExport_withoutProductId_success() {
        // salesOrder1's product has category, salesOrder2's product does not
        when(salesOrderRepository.findAllByOrderDateBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(salesOrder1, salesOrder2));

        List<SalesDataExportDTO> result = dataExportService.getSalesDataForExport(startDate, endDate, Optional.empty());

        assertNotNull(result);
        assertEquals(2, result.size());

        SalesDataExportDTO dto1 = result.stream().filter(dto -> dto.getOrderId().equals(salesOrder1.getId())).findFirst().orElse(null);
        assertNotNull(dto1);
        assertEquals(category1.getName(), dto1.getCategoryName());

        SalesDataExportDTO dto2 = result.stream().filter(dto -> dto.getOrderId().equals(salesOrder2.getId())).findFirst().orElse(null);
        assertNotNull(dto2);
        assertNull(dto2.getCategoryName()); // Assert category name is null for product2

        verify(salesOrderRepository, never()).findByProductIdAndOrderDateBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(salesOrderRepository).findAllByOrderDateBetween(eq(startDateTime), eq(endDateTime));
    }

    @Test
    void getSalesDataForExport_emptyResult_withProductId() {
        when(salesOrderRepository.findByProductIdAndOrderDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<SalesDataExportDTO> result = dataExportService.getSalesDataForExport(startDate, endDate, Optional.of(1L));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(salesOrderRepository).findByProductIdAndOrderDateBetween(eq(1L), eq(startDateTime), eq(endDateTime));
    }

    @Test
    void getSalesDataForExport_emptyResult_withoutProductId() {
        when(salesOrderRepository.findAllByOrderDateBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<SalesDataExportDTO> result = dataExportService.getSalesDataForExport(startDate, endDate, Optional.empty());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(salesOrderRepository).findAllByOrderDateBetween(eq(startDateTime), eq(endDateTime));
    }
}
