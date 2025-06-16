package com.example.inventorypoc.controller;

import com.example.inventorypoc.model.CurrentStock;
import com.example.inventorypoc.model.InventoryMovement;
import com.example.inventorypoc.model.Product;
import com.example.inventorypoc.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryMovement movementIn;
    private InventoryMovement movementOut;
    private CurrentStock currentStock;
    private Product product;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime serialization

        product = new Product(1L, "Test Product", "Desc", "Cat", 100.0);

        movementIn = new InventoryMovement(1L, product.getId(), InventoryMovement.MovementType.IN, 10, LocalDateTime.now(), "Stock In");
        movementOut = new InventoryMovement(2L, product.getId(), InventoryMovement.MovementType.OUT, 5, LocalDateTime.now(), "Stock Out");
        currentStock = new CurrentStock(product.getId(), 50, LocalDateTime.now());
    }

    @Test
    void recordMovement_shouldReturnCreatedMovement() throws Exception {
        when(inventoryService.recordMovement(any(InventoryMovement.class))).thenReturn(movementIn);

        mockMvc.perform(post("/api/inventory/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movementIn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(product.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(10)));
    }

    @Test
    void recordMovement_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        InventoryMovement invalidMovement = new InventoryMovement(null, 99L, InventoryMovement.MovementType.OUT, 100, LocalDateTime.now(), "Bad one");
        when(inventoryService.recordMovement(any(InventoryMovement.class))).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/inventory/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMovement)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product not found"));
    }


    @Test
    void getCurrentStock_whenStockExists_shouldReturnStock() throws Exception {
        when(inventoryService.getCurrentStock(product.getId())).thenReturn(Optional.of(currentStock));

        mockMvc.perform(get("/api/inventory/stock/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(product.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(50)));
    }

    @Test
    void getCurrentStock_whenStockDoesNotExist_shouldReturnNotFound() throws Exception {
        when(inventoryService.getCurrentStock(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/stock/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMovementsForProduct_shouldReturnListOfMovements() throws Exception {
        List<InventoryMovement> movements = Arrays.asList(movementIn, movementOut);
        when(inventoryService.getMovementsForProduct(product.getId())).thenReturn(movements);

        mockMvc.perform(get("/api/inventory/movements/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("0.quantity", is(movementIn.getQuantity())))
                .andExpect(jsonPath("1.quantity", is(movementOut.getQuantity())));
    }
}
