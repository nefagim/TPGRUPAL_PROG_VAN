package com.empresa.demostockapp.integration;

import com.empresa.demostockapp.dto.category.CategoryRequestDTO;
import com.empresa.demostockapp.dto.product.ProductRequestDTO; // Assuming this DTO path from previous tasks. Will correct if different.
// Correcting DTO path based on actual structure from previous steps:
// import com.empresa.demostockapp.dto.ProductRequestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set; // For SignupRequest roles

// DTOs - actual paths might need adjustment based on project structure
import com.empresa.demostockapp.dto.LoginRequest;
import com.empresa.demostockapp.dto.SignupRequest;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue; // For checking null category


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional // Roll back transactions after each test
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // To run tests in a specific order for lifecycle
public class ProductCategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Placeholder for JWT token. In a real test, this would be obtained dynamically.
    private static String ADMIN_TOKEN;
    private static Long createdCategoryId;
    private static Long createdProductId;

    // Helper to extract JWT token from signin response
    private String extractToken(MvcResult result) throws Exception {
        String responseString = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseString);
        return root.path("token").asText();
    }

    // Helper to extract ID from response
    private Long extractId(MvcResult result) throws Exception {
        String responseString = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseString);
        return root.path("id").asLong();
    }


    @Test
    @Order(1)
    void signupAndSigninAdminUser() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testAdminUserLifeCycle");
        signupRequest.setEmail("testAdminUserLifeCycle@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_ADMIN")); // Assuming roles are set directly like this

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk()); // Or isCreated depending on your signup endpoint

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testAdminUserLifeCycle");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        ADMIN_TOKEN = extractToken(result);
        assertNotNull(ADMIN_TOKEN);
        assertFalse(ADMIN_TOKEN.isEmpty());
    }


    @Test
    @Order(2)
    void testCreateCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available. Ensure signupAndSigninAdminUser runs first and succeeds.");
        CategoryRequestDTO categoryDto = new CategoryRequestDTO("Integration Test Category", "Category for integration testing");

        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Integration Test Category")))
                .andReturn();
        createdCategoryId = extractId(result);
        assertNotNull(createdCategoryId);
    }

    @Test
    @Order(3)
    void testCreateProductWithCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdCategoryId, "Category ID not available. Ensure testCreateCategory runs first.");

        // Corrected ProductRequestDTO import path based on previous steps.
        com.empresa.demostockapp.dto.ProductRequestDTO productDto = new com.empresa.demostockapp.dto.ProductRequestDTO();
        productDto.setName("Integration Product");
        productDto.setDescription("Product for integration test with category");
        productDto.setPrice(BigDecimal.valueOf(99.99));
        productDto.setSku("INT-SKU-001");
        productDto.setCategoryId(createdCategoryId);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Integration Product")))
                .andExpect(jsonPath("$.category.id", is(createdCategoryId.intValue())))
                .andExpect(jsonPath("$.quantity", is(0))) // Assert default quantity
                .andReturn();
        createdProductId = extractId(result);
        assertNotNull(createdProductId);
    }

    @Test
    @Order(4)
    void testGetProductAndVerifyCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");
        assertNotNull(createdCategoryId, "Category ID not available.");

        mockMvc.perform(get("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Integration Product")))
                .andExpect(jsonPath("$.category.id", is(createdCategoryId.intValue())))
                .andExpect(jsonPath("$.category.name", is("Integration Test Category")))
                .andExpect(jsonPath("$.quantity", is(0))); // Assert default quantity
    }

    @Test
    @Order(5)
    void testUpdateProductToNoCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");

        com.empresa.demostockapp.dto.ProductRequestDTO updatedProductDto = new com.empresa.demostockapp.dto.ProductRequestDTO();
        updatedProductDto.setName("Updated Integration Product");
        updatedProductDto.setDescription("Updated description");
        updatedProductDto.setPrice(BigDecimal.valueOf(109.99));
        updatedProductDto.setSku("INT-SKU-001"); // Keep SKU same or change as needed
        updatedProductDto.setCategoryId(null); // Unassign category

        mockMvc.perform(put("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Integration Product")))
                .andExpect(jsonPath("$.quantity", is(0))) // Quantity should be unaffected
                .andExpect(jsonPath("$.category", is(nullValue())));
    }

    @Test
    @Order(6)
    void testGetProductAndVerifyNoCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");

        mockMvc.perform(get("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Integration Product")))
                .andExpect(jsonPath("$.quantity", is(0))) // Quantity should be unaffected
                .andExpect(jsonPath("$.category", is(nullValue())));
    }

    @Test
    @Order(7)
    void testUpdateProductToDifferentCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");

        // Create a new category first
        CategoryRequestDTO anotherCategoryDto = new CategoryRequestDTO("Another Category", "A different category");
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherCategoryDto)))
                .andExpect(status().isCreated())
                .andReturn();
        Long anotherCategoryId = extractId(catResult);


        com.empresa.demostockapp.dto.ProductRequestDTO updatedProductDto = new com.empresa.demostockapp.dto.ProductRequestDTO();
        updatedProductDto.setName("Product With Another Category");
        updatedProductDto.setSku("INT-SKU-001");
        updatedProductDto.setPrice(BigDecimal.valueOf(119.99));
        updatedProductDto.setCategoryId(anotherCategoryId);

        mockMvc.perform(put("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Product With Another Category")))
                .andExpect(jsonPath("$.quantity", is(0))) // Quantity should be unaffected
                .andExpect(jsonPath("$.category.id", is(anotherCategoryId.intValue())))
                .andExpect(jsonPath("$.category.name", is("Another Category")));

        // Clean up the "Another Category" - this makes test order more complex for cleanup
        // It might be better to delete it in a separate @AfterAll or a final @Order test
    }


    @Test
    @Order(8)
    void testDeleteProduct() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");

        mockMvc.perform(delete("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void testGetDeletedProduct() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdProductId, "Product ID not available.");

        mockMvc.perform(get("/api/products/" + createdProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void testDeleteCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdCategoryId, "Category ID not available.");
        // Product associated with createdCategoryId was deleted in Order(8)
        // So this category should now be deletable.

        mockMvc.perform(delete("/api/categories/" + createdCategoryId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(11)
    void testGetDeletedCategory() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(createdCategoryId, "Category ID not available.");

        mockMvc.perform(get("/api/categories/" + createdCategoryId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    // Test for deleting a category that is in use (before product is deleted)
    @Test
    @Order(12) // This needs to run after a product is created with a category, but before that product is deleted.
               // To do this cleanly, we need another category and product.
    void testDeleteCategoryInUse() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");

        // 1. Create a new category
        CategoryRequestDTO catDto = new CategoryRequestDTO("Category In Use Test", "Test");
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catDto)))
                .andExpect(status().isCreated())
                .andReturn();
        Long categoryInUseId = extractId(catResult);

        // 2. Create a product associated with this category
        com.empresa.demostockapp.dto.ProductRequestDTO prodDto = new com.empresa.demostockapp.dto.ProductRequestDTO();
        prodDto.setName("Product For Category In Use Test");
        prodDto.setSku("SKU-IN-USE");
        prodDto.setPrice(BigDecimal.TEN);
        prodDto.setCategoryId(categoryInUseId);
        MvcResult prodResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", is(0))) // Assert default quantity
                .andReturn();
        Long productUsingCatId = extractId(prodResult);


        // 3. Attempt to delete the category (should fail as it's in use)
        mockMvc.perform(delete("/api/categories/" + categoryInUseId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isConflict()); // Expecting 409 Conflict due to IllegalStateException

        // 4. Clean up: Delete the product, then the category
        mockMvc.perform(delete("/api/products/" + productUsingCatId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/categories/" + categoryInUseId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());

        // Clean up "Another Category" if it was created in testOrder(7) and not cleaned up there
        // This shows the difficulty of inter-test cleanup with @Order.
        // A more robust approach might involve @AfterEach or @AfterAll if state isn't static.
        // Or, ensure each @Order test cleans up what it creates if it's not part of the main lifecycle flow.
    }
}
