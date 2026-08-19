package com.hinchmart.controller;

import com.hinchmart.dto.request.ProductCreateRequest;
import com.hinchmart.dto.request.ProductUpdateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.BrandDto;
import com.hinchmart.dto.response.ProductDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Products & Brands", description = "Endpoints for B2B Product Catalog, Bulk Pricing Tiers, and Brands")
public class ProductController {

    private final ProductService productService;
    private final AuthService authService;

    public ProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping("/products")
    @Operation(summary = "Get Products List with Filtering & Pagination",
            description = "Search and filter products by category, subcategory, brand, seller, price range, and stock availability with full bulk pricing tiers.")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<ProductDto> products = productService.getProducts(
                query, categoryId, subcategoryId, brandId, sellerId, minPrice, maxPrice, inStock, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get Product Details by ID", description = "Returns full B2B product specifications, tiered bulk prices, images, and seller info.")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/products/slug/{slug}")
    @Operation(summary = "Get Product Details by Slug", description = "Returns product details by URL slug.")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySlug(@PathVariable String slug) {
        ProductDto product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create Product (Seller / Admin)", description = "Adds a new product with unit MOQ, GST %, and multi-tier bulk pricing.")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(Authentication authentication,
                                                                 @Valid @RequestBody ProductCreateRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        ProductDto created = productService.createProduct(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update Product (Seller / Admin)", description = "Updates an existing product catalog entry.")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(Authentication authentication,
                                                                 @PathVariable Long id,
                                                                 @RequestBody ProductUpdateRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        ProductDto updated = productService.updateProduct(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @GetMapping("/brands")
    @Operation(summary = "List all Brands", description = "Returns all active marketplace brands.")
    public ResponseEntity<ApiResponse<List<BrandDto>>> getAllBrands() {
        List<BrandDto> brands = productService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(brands));
    }
}
