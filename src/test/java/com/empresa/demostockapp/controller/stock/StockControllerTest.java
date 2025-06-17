package com.empresa.demostockapp.controller.stock;

import com.empresa.demostockapp.dto.stock.StockItemResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.stock.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;


@WebMvcTest(StockController.class)
@WithMockUser(roles = {"MANAGER", "ADMIN"}) // Roles as per WebSecurityConfig
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // For security context

    @MockBean
    private JwtUtils jwtUtils; // For security context

    @Autowired
    private ObjectMapper objectMapper;

    private StockItemResponseDTO stockResponseDTO;
    private UpdateStockQuantityRequestDTO updateStockRequestDTO;

    @BeforeEach
    void setUp() {
        stockResponseDTO = new StockItemResponseDTO(1L, "Test Product", 100, LocalDateTime.now());
        updateStockRequestDTO = new UpdateStockQuantityRequestDTO(150);
    }

    @Test
    void getStockByProductId_success() throws Exception {
        when(stockService.getStockByProductId(1L)).thenReturn(stockResponseDTO);

        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.productName", is("Test Product")))
                .andExpect(jsonPath("$.quantity", is(100)));
    }

    @Test
    void getStockByProductId_notFound() throws Exception {
        when(stockService.getStockByProductId(1L)).thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }

    @Test
    void updateStockQuantity_success() throws Exception {
        StockItemResponseDTO updatedResponse = new StockItemResponseDTO(1L, "Test Product", 150, LocalDateTime.now());
        when(stockService.updateStockQuantity(anyLong(), any(UpdateStockQuantityRequestDTO.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/stock/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.quantity", is(150)));
    }

    @Test
    void updateStockQuantity_productNotFound() throws Exception {
        when(stockService.updateStockQuantity(anyLong(), any(UpdateStockQuantityRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(put("/api/stock/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }

    @Test
    void updateStockQuantity_validationError_nullQuantity() throws Exception {
        UpdateStockQuantityRequestDTO invalidRequest = new UpdateStockQuantityRequestDTO(null);

        mockMvc.perform(put("/api/stock/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // GlobalExceptionHandler should handle MethodArgumentNotValidException
    }

    @Test
    void updateStockQuantity_validationError_negativeQuantity() throws Exception {
        UpdateStockQuantityRequestDTO invalidRequest = new UpdateStockQuantityRequestDTO(-10);

        mockMvc.perform(put("/api/stock/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
