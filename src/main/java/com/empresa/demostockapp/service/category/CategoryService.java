package com.empresa.demostockapp.service.category;

import com.empresa.demostockapp.dto.category.CategoryRequestDTO;
import com.empresa.demostockapp.dto.category.CategoryResponseDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Category;
import com.empresa.demostockapp.repository.CategoryRepository;
import com.empresa.demostockapp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // For checking usage before deletion

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        String categoryName = requestDTO.getName().trim();
        if (categoryRepository.existsByName(categoryName)) {
            throw new IllegalArgumentException("Category name '" + categoryName + "' already exists");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription(requestDTO.getDescription());
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.fromCategory(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDTO::fromCategory)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryResponseDTO.fromCategory(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String newName = requestDTO.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Category name '" + newName + "' already exists for another category.");
        }

        category.setName(newName);
        category.setDescription(requestDTO.getDescription());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.fromCategory(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Category '" + category.getName() + "' is in use by products and cannot be deleted.");
        }
        categoryRepository.delete(category);
    }
}
