package com.novacart.store.service;

import com.novacart.store.dto.CategoryDtos;
import com.novacart.store.entity.Category;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDtos.CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(CategoryDtos.CategoryResponse::from)
                .toList();
    }

    public Category requireById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
