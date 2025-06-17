package com.empresa.demostockapp.integration;

import com.empresa.demostockapp.dto.LoginRequest;
import com.empresa.demostockapp.dto.SignupRequest;
import com.empresa.demostockapp.dto.category.CategoryRequestDTO;
import com.empresa.demostockapp.dto.ProductRequestDTO; // Corrected path
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.dto.sales.SalesOrderRequestDTO;

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
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SalesStockExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String ADMIN_TOKEN;
    private static Long testCategoryId;
    private static String testCategoryName;
    private static Long testProductId;
    private static Long testSalesOrderId;
    private static final BigDecimal INITIAL_PRODUCT_PRICE = BigDecimal.valueOf(19.99);


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

    private String extractName(MvcResult result) throws Exception {
        String responseString = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseString);
        return root.path("name").asText();
    }

    @Test
    @Order(1)
    void signupAndSigninAdminUser() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testAdminSalesCycle");
        signupRequest.setEmail("testAdminSalesCycle@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_ADMIN", "ROLE_MANAGER")); // Ensure roles for all operations

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testAdminSalesCycle");
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
    void test_setupProductWithInitialStock() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");

        // Create Category
        CategoryRequestDTO categoryDto = new CategoryRequestDTO("Sales Test Category", "Category for sales testing");
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andReturn();
        testCategoryId = extractId(catResult);
        testCategoryName = extractName(catResult);
        assertNotNull(testCategoryId);

        // Create Product
        ProductRequestDTO productDto = new ProductRequestDTO();
        productDto.setName("Sales Test Product");
        productDto.setDescription("Product for sales integration test");
        productDto.setPrice(INITIAL_PRODUCT_PRICE);
        productDto.setSku("SALE-SKU-001");
        productDto.setCategoryId(testCategoryId);

        MvcResult prodResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andReturn();
        testProductId = extractId(prodResult);
        assertNotNull(testProductId);

        // Set Initial Stock
        UpdateStockQuantityRequestDTO stockDto = new UpdateStockQuantityRequestDTO(100);
        mockMvc.perform(put("/api/stock/" + testProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(testProductId.intValue())))
                .andExpect(jsonPath("$.quantity", is(100)));
    }

    @Test
    @Order(3)
    void test_recordSaleAndVerifyStockAndExport() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(testProductId, "Product ID for sales test not available.");
        assertNotNull(testCategoryName, "Category name for sales test not available.");

        // Record Sales Order
        SalesOrderRequestDTO salesOrderDto = new SalesOrderRequestDTO();
        salesOrderDto.setProductId(testProductId);
        salesOrderDto.setQuantitySold(10);
        salesOrderDto.setSellingPrice(INITIAL_PRODUCT_PRICE); // Use initial price or a new one

        MvcResult saleResult = mockMvc.perform(post("/api/salesorders")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesOrderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(testProductId.intValue())))
                .andExpect(jsonPath("$.quantitySold", is(10)))
                .andReturn();
        testSalesOrderId = extractId(saleResult); // Store sales order ID if needed for direct verification
        assertNotNull(testSalesOrderId);

        // Verify Stock Update
        mockMvc.perform(get("/api/stock/" + testProductId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(90))); // 100 - 10

        // Verify Data Export
        String today = LocalDate.now().toString();
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", today)
                        .param("endDate", today)
                        .param("productId", testProductId.toString())
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(testSalesOrderId.intValue())))
                .andExpect(jsonPath("$[0].productId", is(testProductId.intValue())))
                .andExpect(jsonPath("$[0].quantitySold", is(10)))
                .andExpect(jsonPath("$[0].sellingPrice", is(INITIAL_PRODUCT_PRICE.doubleValue())))
                .andExpect(jsonPath("$[0].categoryName", is(testCategoryName)));
    }

    @Test
    @Order(4)
    void test_recordSale_insufficientStock() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        assertNotNull(testProductId, "Product ID for sales test not available.");
        // Current stock is 90 from previous test.

        SalesOrderRequestDTO salesOrderDto = new SalesOrderRequestDTO();
        salesOrderDto.setProductId(testProductId);
        salesOrderDto.setQuantitySold(100); // More than available (90)
        salesOrderDto.setSellingPrice(INITIAL_PRODUCT_PRICE);

        mockMvc.perform(post("/api/salesorders")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesOrderDto)))
                .andExpect(status().isConflict()); // HTTP 409 for InsufficientStockException
    }
     @Test
    @Order(5)
    void test_exportAllSalesForDateRange() throws Exception {
        assertNotNull(ADMIN_TOKEN, "Admin token not available.");
        // This test assumes salesOrder1 (testSalesOrderId) from Order(3) is the only one for today
        // for this product. If other tests create sales, this might need adjustment or broader date range.
        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", today)
                        .param("endDate", today)
                        // No productId, should fetch all sales in range
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderId == "+testSalesOrderId+")]", hasSize(1))) // Check if our specific order is present
                .andExpect(jsonPath("$[?(@.orderId == "+testSalesOrderId+")].productId", is(List.of(testProductId.intValue()))))
                .andExpect(jsonPath("$[?(@.orderId == "+testSalesOrderId+")].categoryName", is(List.of(testCategoryName))));
    }
}
