package com.empresa.demostockapp.service.category;

import com.empresa.demostockapp.dto.category.CategoryRequestDTO;
import com.empresa.demostockapp.dto.category.CategoryResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category;
import com.empresa.demostockapp.repository.CategoryRepository;
import com.empresa.demostockapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1, category2;
    private CategoryRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        category1 = new Category("Electronics", "Electronic gadgets and devices");
        category1.setId(1L);

        category2 = new Category("Books", "Various genres of books");
        category2.setId(2L);

        requestDTO = new CategoryRequestDTO("New Category", "Description for new category");
    }

    @Test
    void createCategory_success() {
        when(categoryRepository.existsByName(requestDTO.getName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(3L); // Simulate ID assignment
            return cat;
        });

        CategoryResponseDTO response = categoryService.createCategory(requestDTO);

        assertNotNull(response);
        assertEquals(requestDTO.getName(), response.getName());
        assertEquals(requestDTO.getDescription(), response.getDescription());
        assertNotNull(response.getId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_nameConflict() {
        when(categoryRepository.existsByName(requestDTO.getName())).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(requestDTO));
        assertEquals("Category name '" + requestDTO.getName() + "' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_success() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));
        List<CategoryResponseDTO> responses = categoryService.getAllCategories();
        assertEquals(2, responses.size());
        assertEquals("Electronics", responses.get(0).getName());
    }

    @Test
    void getAllCategories_empty() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        List<CategoryResponseDTO> responses = categoryService.getAllCategories();
        assertTrue(responses.isEmpty());
    }

    @Test
    void getCategoryById_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        CategoryResponseDTO response = categoryService.getCategoryById(1L);
        assertEquals("Electronics", response.getName());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void updateCategory_success() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Updated Electronics", "Updated desc");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByName("Updated Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category1); // Mock save to return the updated category

        CategoryResponseDTO response = categoryService.updateCategory(1L, updateRequest);

        assertEquals("Updated Electronics", response.getName());
        assertEquals("Updated desc", response.getDescription());
        verify(categoryRepository).save(category1);
    }

    @Test
    void updateCategory_success_sameName() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO(category1.getName(), "Updated desc only");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        // existsByName should not be called if name is not changing
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);

        CategoryResponseDTO response = categoryService.updateCategory(1L, updateRequest);

        assertEquals(category1.getName(), response.getName());
        assertEquals("Updated desc only", response.getDescription());
        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository).save(category1);
    }


    @Test
    void updateCategory_notFound() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Update", "Desc");
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, updateRequest));
    }

    @Test
    void updateCategory_nameConflict() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Books", "Trying to use existing name");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1)); // Current is "Electronics"
        when(categoryRepository.existsByName("Books")).thenReturn(true); // "Books" already exists for category2

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.updateCategory(1L, updateRequest));
        assertEquals("Category name 'Books' already exists for another category.", exception.getMessage());
    }

    @Test
    void deleteCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false); // Not in use
        doNothing().when(categoryRepository).delete(category1);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category1);
    }

    @Test
    void deleteCategory_notFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    void deleteCategory_inUse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true); // Category is in use

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> categoryService.deleteCategory(1L));
        assertEquals("Category '" + category1.getName() + "' is in use by products and cannot be deleted.", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
