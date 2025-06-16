package com.example.inventorypoc;

import com.example.inventorypoc.controller.UserRegistrationDto; // Assuming this DTO is public or moved
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Define UserRegistrationDto locally if not accessible
    static class TestUserRegistrationDto {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }


    @Test
    void getProducts_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getProducts_authenticatedAsUser_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"}) // ADMIN also has USER role in config
    void getProducts_authenticatedAsAdmin_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void getInventoryStock_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/inventory/stock/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getInventoryStock_authenticated_shouldReturnOkOrNotFound() throws Exception {
        // This will be 404 if product 1 doesn't exist, or OK if it does (and stock entry exists)
        // The point is to check security, not data presence for this specific test.
        mockMvc.perform(get("/api/inventory/stock/1"))
                .andExpect(status().isNotFound()); // Or .isOk() if product 1 had stock
    }

    @Test
    void registerUserEndpoint_shouldBeAccessible() throws Exception {
        TestUserRegistrationDto registrationDto = new TestUserRegistrationDto();
        registrationDto.setUsername("integrationtestuser");
        registrationDto.setPassword("password");

        // This endpoint should be permitted as per SecurityConfig
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated()); // Assuming user doesn't exist
    }

     @Test
    void h2Console_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk()); // Or a redirect if not configured with a specific page
    }
}
