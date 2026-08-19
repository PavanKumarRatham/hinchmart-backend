package com.hinchmart.controller;

import com.hinchmart.dto.request.CategoryCreateRequest;
import com.hinchmart.dto.request.SubcategoryCreateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.CategoryDto;
import com.hinchmart.dto.response.SubcategoryDto;
import com.hinchmart.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Categories & Subcategories", description = "Endpoints for B2B Catalog Hierarchy and Admin Management")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ==========================================
    // Public Catalog APIs
    // ==========================================

    @GetMapping("/categories")
    @Operation(summary = "List all Active Categories", description = "Returns active categories with their associated subcategories.")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get Category by ID", description = "Returns category details and subcategories.")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/subcategories")
    @Operation(summary = "List Subcategories", description = "Returns all subcategories or filters by categoryId.")
    public ResponseEntity<ApiResponse<List<SubcategoryDto>>> getSubcategories(
            @RequestParam(required = false) Long categoryId) {
        List<SubcategoryDto> subcategories = categoryService.getSubcategories(categoryId);
        return ResponseEntity.ok(ApiResponse.success(subcategories));
    }

    // ==========================================
    // Admin Category Management APIs (Member 5)
    // ==========================================

    @GetMapping("/admin/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "List all Categories for Admin (including inactive)", description = "Returns all categories for admin management.")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategoriesForAdmin() {
        List<CategoryDto> categories = categoryService.getAllCategoriesForAdmin();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add Category (Admin)", description = "Creates a new category in the catalog.")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryDto created = categoryService.createCategory(request);
        return new ResponseEntity<>(ApiResponse.success("Category created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Edit Category (Admin)", description = "Updates an existing category.")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request) {
        CategoryDto updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @PatchMapping("/admin/categories/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Activate / Deactivate Category (Admin)", description = "Toggles category active status.")
    public ResponseEntity<ApiResponse<CategoryDto>> toggleCategoryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        CategoryDto updated = categoryService.toggleCategoryStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Category status updated successfully", updated));
    }

    @PutMapping("/admin/categories/{id}/order")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Change Category Display Order (Admin)", description = "Updates display order sequence.")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategoryOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        int order = body.getOrDefault("displayOrder", 0);
        CategoryDto updated = categoryService.updateCategoryOrder(id, order);
        return ResponseEntity.ok(ApiResponse.success("Category display order updated successfully", updated));
    }

    @PutMapping("/admin/categories/{id}/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload / Update Category Image URL (Admin)", description = "Updates the category image URL.")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategoryImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        CategoryDto updated = categoryService.updateCategoryImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Category image updated successfully", updated));
    }

    @PostMapping("/admin/categories/{id}/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add Subcategory to Category (Admin)", description = "Creates a new subcategory.")
    public ResponseEntity<ApiResponse<SubcategoryDto>> createSubcategory(
            @PathVariable Long id,
            @Valid @RequestBody SubcategoryCreateRequest request) {
        request.setCategoryId(id);
        SubcategoryDto created = categoryService.createSubcategory(request);
        return new ResponseEntity<>(ApiResponse.success("Subcategory created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/admin/subcategories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Edit Subcategory (Admin)", description = "Updates an existing subcategory.")
    public ResponseEntity<ApiResponse<SubcategoryDto>> updateSubcategory(
            @PathVariable Long id,
            @RequestBody SubcategoryCreateRequest request) {
        SubcategoryDto updated = categoryService.updateSubcategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subcategory updated successfully", updated));
    }

    @PatchMapping("/admin/subcategories/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Activate / Deactivate Subcategory (Admin)", description = "Toggles subcategory active status.")
    public ResponseEntity<ApiResponse<SubcategoryDto>> toggleSubcategoryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        SubcategoryDto updated = categoryService.toggleSubcategoryStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Subcategory status updated successfully", updated));
    }
}
