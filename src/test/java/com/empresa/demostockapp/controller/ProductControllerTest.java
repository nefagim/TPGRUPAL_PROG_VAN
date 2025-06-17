package com.empresa.demostockapp.controller;

import com.empresa.demostockapp.dto.ProductRequestDTO;
import com.empresa.demostockapp.dto.ProductResponseDTO;
import com.empresa.demostockapp.dto.category.CategoryResponseDTO; // Added
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
import java.util.List; // Added

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue; // Added


@WebMvcTest(ProductController.class)
@WithMockUser // Apply mock user for all tests in this class
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequestDTO productRequestDTO;
    private ProductRequestDTO productRequestWithCategoryDTO;
    private ProductResponseDTO productResponseDTO;
    private ProductResponseDTO productResponseWithCategoryDTO;
    private CategoryResponseDTO categoryResponseDTO;


    @BeforeEach
    void setUp() {
        // DTO for requests without category
        productRequestDTO = new ProductRequestDTO("Test Product", "Description", BigDecimal.valueOf(100.00), "SKU123", null);

        // DTO for requests with category
        productRequestWithCategoryDTO = new ProductRequestDTO("Test Product with Cat", "Cat Description", BigDecimal.valueOf(120.00), "SKU456", 10L);

        // DTO for responses without category
        productResponseDTO = new ProductResponseDTO(); // Using default constructor, then setters
        productResponseDTO.setId(1L);
        productResponseDTO.setName("Test Product");
        productResponseDTO.setDescription("Description");
        productResponseDTO.setPrice(BigDecimal.valueOf(100.00));
        productResponseDTO.setSku("SKU123");
        productResponseDTO.setCategory(null);

        // DTO for responses with category
        categoryResponseDTO = new CategoryResponseDTO(10L, "Electronics", "Electronic devices");
        productResponseWithCategoryDTO = new ProductResponseDTO();
        productResponseWithCategoryDTO.setId(2L);
        productResponseWithCategoryDTO.setName("Test Product with Cat");
        productResponseWithCategoryDTO.setPrice(BigDecimal.valueOf(120.00));
        productResponseWithCategoryDTO.setSku("SKU456");
        productResponseWithCategoryDTO.setCategory(categoryResponseDTO);

    }

    // --- CreateProduct Tests (POST /api/products) ---
    @Test
    void createProduct_success_withoutCategory() throws Exception {
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.category", is(nullValue())));
    }

    @Test
    void createProduct_success_withCategory() throws Exception {
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponseWithCategoryDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestWithCategoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Test Product with Cat")))
                .andExpect(jsonPath("$.category.id", is(10)))
                .andExpect(jsonPath("$.category.name", is("Electronics")));
    }

    @Test
    void createProduct_invalidCategoryId_returnsNotFound() throws Exception {
        when(productService.createProduct(any(ProductRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Category not found with id: " + productRequestWithCategoryDTO.getCategoryId()));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestWithCategoryDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + productRequestWithCategoryDTO.getCategoryId()));
    }


    // --- GetAllProducts Test (GET /api/products) ---
    @Test
    void getAllProducts_success() throws Exception {
        // Assume one product with category, one without
        ProductResponseDTO p1 = new ProductResponseDTO(); p1.setId(1L); p1.setName("P1"); p1.setCategory(null);
        ProductResponseDTO p2 = new ProductResponseDTO(); p2.setId(2L); p2.setName("P2");
        p2.setCategory(new CategoryResponseDTO(10L, "CatName", null));

        when(productService.getAllProducts()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("P1")))
                .andExpect(jsonPath("$[0].category", is(nullValue())))
                .andExpect(jsonPath("$[1].name", is("P2")))
                .andExpect(jsonPath("$[1].category.name", is("CatName")));
    }

    // --- GetProductById Tests (GET /api/products/{id}) ---
    @Test
    void getProductById_success_withCategory() throws Exception {
        when(productService.getProductById(2L)).thenReturn(productResponseWithCategoryDTO);

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Test Product with Cat")))
                .andExpect(jsonPath("$.category.id", is(10)));
    }

    @Test
    void getProductById_success_withoutCategory() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponseDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.category", is(nullValue())));
    }


    @Test
    void getProductById_notFound() throws Exception { // No change needed here
        when(productService.getProductById(1L)).thenThrow(new ResourceNotFoundException("Product not found with id: 1"));
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    // --- UpdateProduct Tests (PUT /api/products/{id}) ---
    @Test
    void updateProduct_success_withCategory() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequestDTO.class))).thenReturn(productResponseWithCategoryDTO);

        mockMvc.perform(put("/api/products/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestWithCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.category.id", is(10)));
    }

    @Test
    void updateProduct_success_setCategoryToNull() throws Exception {
        // Request to update product 2 (which had a category) to have no category
        ProductRequestDTO reqToNullCat = new ProductRequestDTO("Updated Name", "Desc", BigDecimal.ONE, "SKU456", null);
        // Response DTO reflects category is now null
        ProductResponseDTO resWithNullCat = new ProductResponseDTO();
        resWithNullCat.setId(2L); resWithNullCat.setName("Updated Name"); resWithNullCat.setCategory(null);

        when(productService.updateProduct(eq(2L), any(ProductRequestDTO.class))).thenReturn(resWithNullCat);

        mockMvc.perform(put("/api/products/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqToNullCat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.category", is(nullValue())));
    }

    @Test
    void updateProduct_invalidCategoryId_returnsNotFound() throws Exception {
         when(productService.updateProduct(anyLong(), any(ProductRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Category not found with id: " + productRequestWithCategoryDTO.getCategoryId()));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestWithCategoryDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + productRequestWithCategoryDTO.getCategoryId()));
    }

    @Test
    void updateProduct_productNotFound() throws Exception { // No change needed here
        when(productService.updateProduct(anyLong(), any(ProductRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isNotFound());
    }

    // --- DeleteProduct Tests (DELETE /api/products/{id}) ---
    // These tests do not need to change as delete operation is not directly affected by category on product.
    @Test
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Product not found with id: 1")).when(productService).deleteProduct(1L);
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound());
    }
}
