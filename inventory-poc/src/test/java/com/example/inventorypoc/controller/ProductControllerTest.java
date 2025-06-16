package com.example.inventorypoc.controller;

import com.example.inventorypoc.model.Product;
import com.example.inventorypoc.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Test Product 1", "Description 1", "Category 1", 10.0);
        product2 = new Product(2L, "Test Product 2", "Description 2", "Category 2", 20.0);
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(product1);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(product1.getName())));
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("0.name", is(product1.getName())))
                .andExpect(jsonPath("1.name", is(product2.getName())));
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product1));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(product1.getName())));
    }

    @Test
    void getProductById_whenProductDoesNotExist_shouldReturnNotFound() throws Exception {
        when(productService.getProductById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_whenProductExists_shouldReturnUpdatedProduct() throws Exception {
        Product updatedProductDetails = new Product(null, "Updated Name", "Updated Desc", "Updated Cat", 15.0);
        Product updatedProduct = new Product(1L, "Updated Name", "Updated Desc", "Updated Cat", 15.0);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProductDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void updateProduct_whenProductDoesNotExist_shouldReturnNotFound() throws Exception {
        Product updatedProductDetails = new Product(null, "Updated Name", "Updated Desc", "Updated Cat", 15.0);
        when(productService.updateProduct(eq(3L), any(Product.class))).thenThrow(new RuntimeException("Product not found"));


        mockMvc.perform(put("/api/products/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProductDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
