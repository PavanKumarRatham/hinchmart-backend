package com.example.hinchmart.service;

import com.example.hinchmart.entity.Category;
import com.example.hinchmart.entity.SubCategory;
import com.example.hinchmart.repository.CategoryRepository;
import com.example.hinchmart.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubCategoryService {

    private final SubCategoryRepository repository;
    private final CategoryRepository categoryRepository;

    public SubCategoryService(SubCategoryRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    public List<SubCategory> getAllSubCategories() {
        return repository.findAll();
    }

    public SubCategory getSubCategoryById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + id));
    }

    public List<SubCategory> getSubCategoriesByCategoryId(Long categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    public SubCategory createSubCategory(SubCategory subCategory) {
        if (subCategory.getCategory() != null && subCategory.getCategory().getId() != null) {
            Category category = categoryRepository.findById(subCategory.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + subCategory.getCategory().getId()));
            subCategory.setCategory(category);
        }
        return repository.save(subCategory);
    }
}
