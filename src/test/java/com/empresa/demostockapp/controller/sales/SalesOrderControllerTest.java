package com.empresa.demostockapp.controller.sales;

import com.empresa.demostockapp.dto.sales.SalesOrderRequestDTO;
import com.empresa.demostockapp.dto.sales.SalesOrderResponseDTO;
import com.empresa.demostockapp.exception.InsufficientStockException;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.sales.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(SalesOrderController.class)
@WithMockUser(roles = {"MANAGER", "ADMIN"}) // Roles as per WebSecurityConfig
class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalesOrderService salesOrderService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // For security context

    @MockBean
    private JwtUtils jwtUtils; // For security context

    @Autowired
    private ObjectMapper objectMapper;

    private SalesOrderRequestDTO salesRequestDTO;
    private SalesOrderResponseDTO salesResponseDTO;

    @BeforeEach
    void setUp() {
        salesRequestDTO = new SalesOrderRequestDTO(1L, 5, BigDecimal.valueOf(25.99));
        salesResponseDTO = new SalesOrderResponseDTO(1L, 1L, "Test Product", 5, BigDecimal.valueOf(25.99), LocalDateTime.now());
    }

    @Test
    void recordSale_success() throws Exception {
        when(salesOrderService.recordSale(any(SalesOrderRequestDTO.class))).thenReturn(salesResponseDTO);

        mockMvc.perform(post("/api/salesorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.quantitySold", is(5)));
    }

    @Test
    void recordSale_productNotFound() throws Exception {
        when(salesOrderService.recordSale(any(SalesOrderRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(post("/api/salesorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }

    @Test
    void recordSale_insufficientStock() throws Exception {
        when(salesOrderService.recordSale(any(SalesOrderRequestDTO.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product: Test Product. Available: 3, Requested: 5"));

        mockMvc.perform(post("/api/salesorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesRequestDTO)))
                .andExpect(status().isConflict()) // 409 as per InsufficientStockException @ResponseStatus
                .andExpect(content().string("Insufficient stock for product: Test Product. Available: 3, Requested: 5"));
    }


    @Test
    void recordSale_validationError_nullProductId() throws Exception {
        SalesOrderRequestDTO invalidDTO = new SalesOrderRequestDTO(null, 5, BigDecimal.valueOf(25.99));
        mockMvc.perform(post("/api/salesorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recordSale_validationError_nonPositiveQuantity() throws Exception {
        SalesOrderRequestDTO invalidDTO = new SalesOrderRequestDTO(1L, 0, BigDecimal.valueOf(25.99));
        mockMvc.perform(post("/api/salesorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getSalesForProduct_success() throws Exception {
        when(salesOrderService.getSalesForProduct(1L)).thenReturn(Collections.singletonList(salesResponseDTO));

        mockMvc.perform(get("/api/salesorders/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId", is(1)));
    }

    @Test
    void getSalesForProduct_productNotFound() throws Exception {
        when(salesOrderService.getSalesForProduct(1L)).thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(get("/api/salesorders/product/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }
}
