package com.empresa.demostockapp.integration;

import com.empresa.demostockapp.dto.LoginRequest;
import com.empresa.demostockapp.dto.SignupRequest;
import com.empresa.demostockapp.repository.UserRepository; // For potential cleanup or direct checks
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // Important for test isolation

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional // Roll back transactions after each test
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository; // Can be used for cleanup if @Transactional is not enough or for setup

    // Helper to extract JWT token from signin response
    private String extractToken(MvcResult result) throws Exception {
        String responseString = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseString);
        return root.path("token").asText();
    }

    // Helper method to perform signup and signin, returns the token
    private String performSignupAndSignin(String username, String email, String password, Set<String> roles) throws Exception {
        SignupRequest signupDto = new SignupRequest();
        signupDto.setUsername(username);
        signupDto.setEmail(email);
        signupDto.setPassword(password);
        signupDto.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isOk()); // Assuming signup returns 200 OK

        LoginRequest loginDto = new LoginRequest();
        loginDto.setUsername(username);
        loginDto.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        return extractToken(loginResult);
    }


    @Test
    void testSignupAndSignin_success() throws Exception {
        SignupRequest signupDto = new SignupRequest();
        signupDto.setUsername("testuserSignupSignin");
        signupDto.setEmail("testuserSignupSignin@example.com");
        signupDto.setPassword("password123");
        signupDto.setRoles(Set.of("USER")); // AuthController signup takes roles like "USER"

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isOk()) // Assuming signup returns 200 OK with a message
                .andExpect(content().string("User registered successfully!"));

        LoginRequest loginDto = new LoginRequest();
        loginDto.setUsername("testuserSignupSignin");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username", is("testuserSignupSignin")))
                .andExpect(jsonPath("$.email", is("testuserSignupSignin@example.com")))
                // UserDetailsImpl adds "ROLE_" prefix, so roles in response are "ROLE_USER"
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER")));
    }

    @Test
    void testAccessAllowedEndpoint_withValidToken() throws Exception {
        String managerToken = performSignupAndSignin("testmanagerAccess", "manageraccess@example.com", "password123", Set.of("MANAGER"));
        assertNotNull(managerToken);

        // /api/categories is accessible to MANAGER or ADMIN
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessForbiddenEndpoint_withInsufficientRoleToken() throws Exception {
        String managerToken = performSignupAndSignin("testmanagerForbidden", "managerforbidden@example.com", "password123", Set.of("MANAGER"));
        assertNotNull(managerToken);

        // /api/admin/users is ADMIN only
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccessAdminEndpoint_withAdminRoleToken() throws Exception {
        String adminToken = performSignupAndSignin("testadminAccess", "adminaccess@example.com", "password123", Set.of("ADMIN"));
        assertNotNull(adminToken);

        // /api/admin/users is ADMIN only
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessSecuredEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/categories")) // Requires MANAGER or ADMIN
                .andExpect(status().isUnauthorized()); // AuthEntryPointJwt should trigger 401
    }

    @Test
    void testAccessSecuredEndpoint_withInvalidToken() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer aninvalidtokenstring"))
                .andExpect(status().isUnauthorized()); // AuthEntryPointJwt should trigger 401
    }

    @Test
    void testSignup_usernameAlreadyExists() throws Exception {
        // First signup
        performSignupAndSignin("existinguser", "existinguser@example.com", "password123", Set.of("USER"));

        // Attempt to signup again with the same username
        SignupRequest duplicateUserDto = new SignupRequest();
        duplicateUserDto.setUsername("existinguser");
        duplicateUserDto.setEmail("anotheremail@example.com"); // Different email
        duplicateUserDto.setPassword("password456");
        duplicateUserDto.setRoles(Set.of("USER"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUserDto)))
                .andExpect(status().isBadRequest()) // AuthController explicitly returns ResponseEntity.badRequest()
                .andExpect(jsonPath("$.body", is("Error: Username is already taken!"))); // Or whatever the message is
    }

    @Test
    void testSignup_emailAlreadyExists() throws Exception {
        // First signup
        performSignupAndSignin("anotheruser", "existingemail@example.com", "password123", Set.of("USER"));

        // Attempt to signup again with the same email
        SignupRequest duplicateEmailDto = new SignupRequest();
        duplicateEmailDto.setUsername("newusername"); // Different username
        duplicateEmailDto.setEmail("existingemail@example.com");
        duplicateEmailDto.setPassword("password456");
        duplicateEmailDto.setRoles(Set.of("USER"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.body", is("Error: Email is already in use!")));
    }
}
