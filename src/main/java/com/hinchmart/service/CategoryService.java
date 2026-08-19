package com.hinchmart.service;

import com.hinchmart.dto.request.CategoryCreateRequest;
import com.hinchmart.dto.request.SubcategoryCreateRequest;
import com.hinchmart.dto.response.CategoryDto;
import com.hinchmart.dto.response.SubcategoryDto;
import com.hinchmart.entity.Category;
import com.hinchmart.entity.Subcategory;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.repository.CategoryRepository;
import com.hinchmart.repository.SubcategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public CategoryService(CategoryRepository categoryRepository, SubcategoryRepository subcategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesForAdmin() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return mapToCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<SubcategoryDto> getSubcategories(Long categoryId) {
        List<Subcategory> subcategories = (categoryId != null) ?
                subcategoryRepository.findByCategoryIdAndIsActiveTrue(categoryId) :
                subcategoryRepository.findByIsActiveTrue();

        return subcategories.stream()
                .map(this::mapToSubcategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category name already exists: " + request.getName());
        }
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty()) ?
                toSlug(request.getSlug()) : toSlug(request.getName());

        Category category = new Category(
                request.getName(),
                slug,
                request.getDescription(),
                request.getImageUrl(),
                request.getDisplayOrder() != null ? request.getDisplayOrder() : 0
        );
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getSlug() != null) {
            category.setSlug(toSlug(request.getSlug()));
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional
    public CategoryDto toggleCategoryStatus(Long id, boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setActive(active);
        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional
    public CategoryDto updateCategoryOrder(Long id, int displayOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setDisplayOrder(displayOrder);
        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional
    public CategoryDto updateCategoryImage(Long id, String imageUrl) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setImageUrl(imageUrl);
        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional
    public SubcategoryDto createSubcategory(SubcategoryCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty()) ?
                toSlug(request.getSlug()) : toSlug(request.getName());

        Subcategory subcategory = new Subcategory(
                category,
                request.getName(),
                slug,
                request.getDescription(),
                request.getImageUrl()
        );
        if (request.getActive() != null) {
            subcategory.setActive(request.getActive());
        }

        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToSubcategoryDto(saved);
    }

    @Transactional
    public SubcategoryDto updateSubcategory(Long id, SubcategoryCreateRequest request) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found with ID: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            subcategory.setCategory(category);
        }
        if (request.getName() != null) {
            subcategory.setName(request.getName());
        }
        if (request.getSlug() != null) {
            subcategory.setSlug(toSlug(request.getSlug()));
        }
        if (request.getDescription() != null) {
            subcategory.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            subcategory.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            subcategory.setActive(request.getActive());
        }

        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToSubcategoryDto(saved);
    }

    @Transactional
    public SubcategoryDto toggleSubcategoryStatus(Long id, boolean active) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found with ID: " + id));
        subcategory.setActive(active);
        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToSubcategoryDto(saved);
    }

    public CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setActive(category.isActive());

        if (category.getSubcategories() != null) {
            dto.setSubcategories(category.getSubcategories().stream()
                    .filter(Subcategory::isActive)
                    .map(this::mapToSubcategoryDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public SubcategoryDto mapToSubcategoryDto(Subcategory sub) {
        SubcategoryDto dto = new SubcategoryDto();
        dto.setId(sub.getId());
        dto.setName(sub.getName());
        dto.setSlug(sub.getSlug());
        dto.setDescription(sub.getDescription());
        dto.setImageUrl(sub.getImageUrl());
        dto.setActive(sub.isActive());
        if (sub.getCategory() != null) {
            dto.setCategoryId(sub.getCategory().getId());
            dto.setCategoryName(sub.getCategory().getName());
        }
        return dto;
    }

    private String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
