package com.empresa.demostockapp.controller.category;

import com.empresa.demostockapp.dto.category.CategoryRequestDTO;
import com.empresa.demostockapp.dto.category.CategoryResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.category.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDTO categoryRequestDTO;
    private CategoryResponseDTO categoryResponseDTO;

    @BeforeEach
    void setUp() {
        categoryRequestDTO = new CategoryRequestDTO("Electronics", "Electronic devices");
        categoryResponseDTO = CategoryResponseDTO.fromCategory(new com.empresa.demostockapp.model.Category("Electronics", "Electronic devices"));
        categoryResponseDTO.setId(1L);
    }

    // POST Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_withAdminRole_success() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDTO.class))).thenReturn(categoryResponseDTO);
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_withAdminRole_validationError() throws Exception {
        CategoryRequestDTO invalidDTO = new CategoryRequestDTO("", null); // Blank name
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_withAdminRole_nameConflict() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Category name already exists"));
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isBadRequest()) // Assuming GlobalExceptionHandler maps IllegalArgumentException to 400
                .andExpect(content().string("Category name already exists"));
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    void createCategory_withManagerRole_forbidden() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isForbidden());
    }

    // GET All Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_withAdminRole_success() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponseDTO));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllCategories_withManagerRole_success() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponseDTO));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "USER") // Assuming USER role exists and should not access
    void getAllCategories_withUserRole_forbidden() throws Exception {
         mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }


    // GET By ID Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_withAdminRole_success() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponseDTO);
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getCategoryById_withManagerRole_success() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponseDTO);
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_withAdminRole_notFound() throws Exception {
        when(categoryService.getCategoryById(1L)).thenThrow(new ResourceNotFoundException("Category not found"));
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());
    }


    // PUT Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_withAdminRole_success() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(CategoryRequestDTO.class))).thenReturn(categoryResponseDTO);
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_withAdminRole_notFound() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(CategoryRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Category not found"));
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_withAdminRole_nameConflict() throws Exception {
         when(categoryService.updateCategory(anyLong(), any(CategoryRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Category name already exists"));
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isBadRequest()); // Or 409 if GlobalExceptionHandler maps it differently
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    void updateCategory_withManagerRole_forbidden() throws Exception {
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isForbidden());
    }

    // DELETE Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_withAdminRole_success() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_withAdminRole_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found")).when(categoryService).deleteCategory(1L);
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_withAdminRole_inUse() throws Exception {
        doThrow(new IllegalStateException("Category is in use")).when(categoryService).deleteCategory(1L);
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict()); // GlobalExceptionHandler maps IllegalStateException to 409
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteCategory_withManagerRole_forbidden() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden());
    }

    // Unauthenticated tests
    @Test
    void createCategory_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void getAllCategories_unauthenticated() throws Exception {
         mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
     @Test
    void getCategoryById_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void updateCategory_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void deleteCategory_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isUnauthorized());
    }

}
