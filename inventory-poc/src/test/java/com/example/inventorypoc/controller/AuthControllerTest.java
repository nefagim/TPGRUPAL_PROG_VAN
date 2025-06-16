package com.example.inventorypoc.controller;

import com.example.inventorypoc.config.SecurityConfig; // To ensure PasswordEncoder bean is available
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class) // Import SecurityConfig to get PasswordEncoder bean
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsManager userDetailsManager; // This is InMemoryUserDetailsManager in SecurityConfig

    @Autowired
    private PasswordEncoder passwordEncoder; // Autowire the actual bean from SecurityConfig

    @Autowired
    private ObjectMapper objectMapper;

    // Helper DTO class for registration payload
    static class UserRegistrationDtoTest {
        private String username;
        private String password;
        // Getters and setters needed for Jackson
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }


    @Test
    void registerUser_success() throws Exception {
        UserRegistrationDtoTest registrationDto = new UserRegistrationDtoTest();
        registrationDto.setUsername("newUser");
        registrationDto.setPassword("newPassword");

        when(userDetailsManager.userExists("newUser")).thenReturn(false);
        // void createUser(UserDetails user);
        // We don't need to mock createUser as it's void and we trust UserDetailsManager to work
        // or we can use doNothing().when(userDetailsManager).createUser(any(UserDetails.class));


        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully: newUser"));
    }

    @Test
    void registerUser_alreadyExists() throws Exception {
        UserRegistrationDtoTest registrationDto = new UserRegistrationDtoTest();
        registrationDto.setUsername("existingUser");
        registrationDto.setPassword("password");

        when(userDetailsManager.userExists("existingUser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists."));
    }

    @Test
    void registerUser_missingUsername_shouldReturnBadRequest() throws Exception {
        UserRegistrationDtoTest registrationDto = new UserRegistrationDtoTest();
        registrationDto.setPassword("password"); // Username is null

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username and password are required."));
    }
}
