package com.empresa.demostockapp.controller;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.ProductService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;


@WebMvcTest(ProductController.class)
@WithMockUser // Apply mock user for all tests in this class
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    // Mock UserDetailsServiceImpl and JwtUtils as they are part of the security setup
    // that WebMvcTest might try to initialize.
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        productRequestDTO = new ProductRequestDTO("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123");
        productResponseDTO = new ProductResponseDTO(1L, "Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123");
    }

    @Test
    void createProduct_success() throws Exception {
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void getAllProducts_success() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(productResponseDTO));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")));
    }

    @Test
    void getProductById_success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponseDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void getProductById_notFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1")); // GlobalExceptionHandler should handle this
    }

    @Test
    void updateProduct_success() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void updateProduct_notFound() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }


    @Test
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound() throws Exception {
        // Mock the service to throw ResourceNotFoundException when deleteProduct is called for a non-existent ID
        // The ProductService itself handles the findById check before deletion.
        // So, we configure deleteProduct to throw the exception if the service's pre-check fails.
        // However, for controller test, we assume service correctly throws if product to delete is not found.
        // The service's deleteProduct method would throw ResourceNotFoundException if findById fails.
        // So we directly mock that behaviour from the service for this controller test.

        // Correct approach: productService.deleteProduct itself might throw if the pre-check (findById) fails.
        // So we mock this behavior:
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Product not found with id: 1")).when(productService).deleteProduct(1L);


        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: 1"));
    }
}
