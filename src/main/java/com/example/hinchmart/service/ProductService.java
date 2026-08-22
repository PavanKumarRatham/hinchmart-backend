package com.example.hinchmart.service;

import com.example.hinchmart.entity.Brand;
import com.example.hinchmart.entity.Category;
import com.example.hinchmart.entity.Product;
import com.example.hinchmart.entity.SubCategory;
import com.example.hinchmart.repository.BrandRepository;
import com.example.hinchmart.repository.CategoryRepository;
import com.example.hinchmart.repository.ProductRepository;
import com.example.hinchmart.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;

    public ProductService(ProductRepository repository,
                          CategoryRepository categoryRepository,
                          SubCategoryRepository subCategoryRepository,
                          BrandRepository brandRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.brandRepository = brandRepository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getProductsByCategoryId(Long categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    public List<Product> getProductsBySubCategoryId(Long subcategoryId) {
        return repository.findBySubcategoryId(subcategoryId);
    }

    public Product createProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));
            product.setCategory(category);
        }
        if (product.getSubcategory() != null && product.getSubcategory().getId() != null) {
            SubCategory subCategory = subCategoryRepository.findById(product.getSubcategory().getId())
                    .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + product.getSubcategory().getId()));
            product.setSubcategory(subCategory);
        }
        if (product.getBrand() != null && product.getBrand().getId() != null) {
            Brand brand = brandRepository.findById(product.getBrand().getId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + product.getBrand().getId()));
            product.setBrand(brand);
        }
        return repository.save(product);
    }
}