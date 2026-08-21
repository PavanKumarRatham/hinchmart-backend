package com.hinchmart.controller;

import com.hinchmart.dto.request.ProductCreateRequest;
import com.hinchmart.dto.request.ProductUpdateRequest;
import com.hinchmart.dto.request.SellerProductStatusRequest;
import com.hinchmart.dto.request.SellerStoreRequest;
import com.hinchmart.dto.response.*;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.ApprovalStatus;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.ProductService;
import com.hinchmart.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@Tag(name = "Seller Operations (Member 1)", description = "Endpoints for Seller Store, Dashboard Metrics, and Product Upload & Management")
public class SellerController {

    private final SellerService sellerService;
    private final ProductService productService;
    private final AuthService authService;

    public SellerController(SellerService sellerService,
                            ProductService productService,
                            AuthService authService) {
        this.sellerService = sellerService;
        this.productService = productService;
        this.authService = authService;
    }

    // ==========================================
    // 1. Seller Store Management
    // ==========================================

    @PostMapping("/store")
    @Operation(summary = "Create Seller Store",
            description = "Creates a seller store. Requires seller status to be APPROVED. If verification is pending, returns an error.")
    public ResponseEntity<ApiResponse<SellerStoreDto>> createStore(@RequestParam Long userId,
                                                                   @Valid @RequestBody SellerStoreRequest request) {
        User user = authService.getUserById(userId);
        SellerStoreDto store = sellerService.createStore(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Store created successfully", store), HttpStatus.CREATED);
    }

    @GetMapping("/store")
    @Operation(summary = "Get Seller Store", description = "Retrieves the specified seller's store profile.")
    public ResponseEntity<ApiResponse<SellerStoreDto>> getStore(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        SellerStoreDto store = sellerService.getStore(user.getId());
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    @PutMapping("/store")
    @Operation(summary = "Update Seller Store", description = "Updates store name, logo, banner, description, and contact info.")
    public ResponseEntity<ApiResponse<SellerStoreDto>> updateStore(@RequestParam Long userId,
                                                                   @RequestBody SellerStoreRequest request) {
        User user = authService.getUserById(userId);
        SellerStoreDto store = sellerService.updateStore(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", store));
    }

    // ==========================================
    // 2. Seller Dashboard Metrics
    // ==========================================

    @GetMapping("/dashboard")
    @Operation(summary = "Get Seller Dashboard Metrics",
            description = "Returns real-time aggregated metrics: Total Products, Active Products, Pending Approval, Total Orders, New Orders, Open RFQs, Revenue, Low Stock Products, Recent Orders.")
    public ResponseEntity<ApiResponse<SellerDashboardDto>> getDashboard(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        SellerDashboardDto dashboard = sellerService.getSellerDashboard(user.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ==========================================
    // 3. Seller Product Management
    // ==========================================

    @PostMapping("/products")
    @Operation(summary = "Upload Product by Seller",
            description = "Submits a new product catalog entry with bulk tiers and MOQ. Created product will have approval_status = PENDING.")
    public ResponseEntity<ApiResponse<ProductDto>> uploadProduct(@RequestParam Long userId,
                                                                 @Valid @RequestBody ProductCreateRequest request) {
        User user = authService.getUserById(userId);
        ProductDto created = productService.createProduct(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Product submitted successfully for admin review", created), HttpStatus.CREATED);
    }

    @GetMapping("/products")
    @Operation(summary = "List Seller Products", description = "Returns all products uploaded by the specified seller with status filtering and pagination.")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getMyProducts(
            @RequestParam Long userId,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        User user = authService.getUserById(userId);
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<ProductDto> products = productService.getSellerProducts(user.getId(), status, isActive, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get Seller Product by ID", description = "Retrieves details of a product uploaded by the seller.")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@RequestParam Long userId,
                                                                 @PathVariable Long id) {
        User user = authService.getUserById(userId);
        ProductDto product = productService.getSellerProductById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update Seller Product", description = "Updates details, pricing, bulk tiers, and stock of a seller product.")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@RequestParam Long userId,
                                                                 @PathVariable Long id,
                                                                 @Valid @RequestBody ProductUpdateRequest request) {
        User user = authService.getUserById(userId);
        ProductDto updated = productService.updateProduct(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete Seller Product", description = "Deletes a product uploaded by the seller.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@RequestParam Long userId,
                                                           @PathVariable Long id) {
        User user = authService.getUserById(userId);
        productService.deleteProduct(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @PatchMapping("/products/{id}/status")
    @Operation(summary = "Toggle Seller Product Active Status", description = "Activates or deactivates a product.")
    public ResponseEntity<ApiResponse<ProductDto>> toggleProductStatus(@RequestParam Long userId,
                                                                      @PathVariable Long id,
                                                                      @Valid @RequestBody SellerProductStatusRequest request) {
        User user = authService.getUserById(userId);
        ProductDto updated = productService.toggleSellerProductStatus(id, user.getId(), request.getActive());
        return ResponseEntity.ok(ApiResponse.success("Product active status updated", updated));
    }
}
