package com.example.hinchmart.controller;


import com.example.hinchmart.entity.SubCategory;
import com.example.hinchmart.service.SubCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
@CrossOrigin
public class SubCategoryController {

    private final SubCategoryService service;

    public SubCategoryController(SubCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<SubCategory> getAllSubCategories() {
        return service.getAllSubCategories();
    }

    @GetMapping("/{id}")
    public SubCategory getSubCategoryById(@PathVariable Long id) {
        return service.getSubCategoryById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<SubCategory> getSubCategoriesByCategoryId(@PathVariable Long categoryId) {
        return service.getSubCategoriesByCategoryId(categoryId);
    }

    @PostMapping
    public ResponseEntity<SubCategory> createSubCategory(@RequestBody SubCategory subCategory) {
        SubCategory savedSubCategory = service.createSubCategory(subCategory);
        return new ResponseEntity<>(savedSubCategory, HttpStatus.CREATED);
    }
}
