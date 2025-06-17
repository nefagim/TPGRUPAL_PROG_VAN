package com.empresa.demostockapp.controller.admin;

import com.empresa.demostockapp.dto.user.UserCreateRequestDTO;
import com.empresa.demostockapp.dto.user.UserDetailResponseDTO;
import com.empresa.demostockapp.dto.user.UserUpdateRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(AdminUserController.class)
@WithMockUser(roles = "ADMIN") // Ensure user has ADMIN role for these tests
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // Required for security context in WebMvcTest

    @MockBean
    private JwtUtils jwtUtils; // Required for security context in WebMvcTest

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailResponseDTO userDetailResponseDTO;
    private UserCreateRequestDTO userCreateRequestDTO;
    private UserUpdateRequestDTO userUpdateRequestDTO;

    @BeforeEach
    void setUp() {
        userDetailResponseDTO = new UserDetailResponseDTO(1L, "testuser", "test@example.com", List.of("ROLE_USER"));

        userCreateRequestDTO = new UserCreateRequestDTO();
        userCreateRequestDTO.setUsername("newuser");
        userCreateRequestDTO.setEmail("new@example.com");
        userCreateRequestDTO.setPassword("password123");
        userCreateRequestDTO.setRoles(Set.of("ROLE_USER"));

        userUpdateRequestDTO = new UserUpdateRequestDTO();
        userUpdateRequestDTO.setEmail("updated@example.com");
        userUpdateRequestDTO.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
    }

    @Test
    void getAllUsers_success() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.singletonList(userDetailResponseDTO));
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")));
    }

    @Test
    void getUserById_success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(userDetailResponseDTO);
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.findUserById(1L)).thenThrow(new ResourceNotFoundException("User not found"));
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void createUser_success() throws Exception {
        when(userService.createUser(any(UserCreateRequestDTO.class))).thenReturn(userDetailResponseDTO);
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void createUser_validationError() throws Exception {
        UserCreateRequestDTO invalidDTO = new UserCreateRequestDTO(); // Missing fields
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest()); // Expect 400 due to validation
    }

    @Test
    void createUser_usernameConflict() throws Exception {
        when(userService.createUser(any(UserCreateRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Error: Username is already taken!"));
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDTO)))
                .andExpect(status().isBadRequest()) // GlobalExceptionHandler maps IllegalArgumentException to 400
                .andExpect(content().string("Error: Username is already taken!"));
    }


    @Test
    void updateUser_success() throws Exception {
        when(userService.updateUser(anyLong(), any(UserUpdateRequestDTO.class))).thenReturn(userDetailResponseDTO);
        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void updateUser_notFound() throws Exception {
        when(userService.updateUser(anyLong(), any(UserUpdateRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("User not found"));
        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_emailConflict() throws Exception {
        when(userService.updateUser(anyLong(), any(UserUpdateRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Error: Email is already in use by another account!"));
        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email is already in use by another account!"));
    }


    @Test
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteUserById(1L);
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUserById(1L);
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNotFound());
    }
}
