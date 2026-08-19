package com.hinchmart.controller;

import com.hinchmart.dto.request.SellerStatusUpdateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.DashboardStatsDto;
import com.hinchmart.dto.response.ProductDto;
import com.hinchmart.dto.response.SellerProfileDto;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.entity.enums.ApprovalStatus;
import com.hinchmart.entity.enums.SellerStatus;
import com.hinchmart.service.ProductService;
import com.hinchmart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Operations", description = "Endpoints for Admin Dashboard, Seller Approvals & KYC, Product Approvals, and User Management")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;

    public AdminController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    // ==========================================
    // 1. Dashboard Metrics (Member 5)
    // ==========================================

    @GetMapping("/dashboard")
    @Operation(summary = "Get Admin Dashboard Statistics",
            description = "Returns real-time aggregated metrics: Total Buyers, Total Sellers, Pending Sellers, Active Products, Open RFQs, Today Orders, Total Categories.")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = userService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==========================================
    // 2. User Management
    // ==========================================

    @GetMapping("/users")
    @Operation(summary = "List all Users", description = "Returns all platform users across roles.")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // ==========================================
    // 3. Seller Management & Approval Workflow (Member 6)
    // ==========================================

    @GetMapping("/sellers")
    @Operation(summary = "List Sellers with Status Filter",
            description = "Returns sellers filtered by status (PENDING, UNDER_REVIEW, APPROVED, REJECTED, SUSPENDED).")
    public ResponseEntity<ApiResponse<List<SellerProfileDto>>> getSellers(
            @RequestParam(required = false) SellerStatus status) {
        List<SellerProfileDto> sellers = userService.getAllSellers(status);
        return ResponseEntity.ok(ApiResponse.success(sellers));
    }

    @GetMapping("/sellers/{id}")
    @Operation(summary = "Get Seller Details by Profile ID",
            description = "Returns comprehensive seller details including contact person, mobile, email, GSTIN, PAN, bank details, and all uploaded KYC documents.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> getSellerById(@PathVariable Long id) {
        SellerProfileDto seller = userService.getSellerProfileById(id);
        return ResponseEntity.ok(ApiResponse.success(seller));
    }

    @PatchMapping("/sellers/{id}/status")
    @Operation(summary = "Update Seller Approval Status", description = "Generic status update endpoint.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> updateSellerStatus(
            @PathVariable Long id,
            @Valid @RequestBody SellerStatusUpdateRequest request) {
        SellerProfileDto updated = userService.updateSellerStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Seller status updated successfully", updated));
    }

    @PatchMapping("/sellers/{id}/approve")
    @Operation(summary = "Approve Seller (Member 6)", description = "Sets seller status to APPROVED, enabling product listing and bidding.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> approveSeller(@PathVariable Long id) {
        SellerProfileDto updated = userService.approveSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller approved successfully", updated));
    }

    @PatchMapping("/sellers/{id}/reject")
    @Operation(summary = "Reject Seller (Member 6)", description = "Sets seller status to REJECTED with rejection reason.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> rejectSeller(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.getOrDefault("rejectionReason", "Documents rejected during KYC check.") : "Documents rejected.";
        SellerProfileDto updated = userService.rejectSeller(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Seller rejected", updated));
    }

    @PatchMapping("/sellers/{id}/request-changes")
    @Operation(summary = "Request Changes on Seller Profile (Member 6)", description = "Sets seller status to UNDER_REVIEW with reviewer notes.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> requestChanges(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = (body != null) ? body.getOrDefault("notes", "Please re-upload clearer copies of your KYC documents.") : "Changes requested.";
        SellerProfileDto updated = userService.requestChangesSeller(id, notes);
        return ResponseEntity.ok(ApiResponse.success("Requested changes from seller", updated));
    }

    @PatchMapping("/sellers/{id}/suspend")
    @Operation(summary = "Suspend Seller Account", description = "Suspends a seller account.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> suspendSeller(@PathVariable Long id) {
        SellerProfileDto updated = userService.suspendSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller account suspended", updated));
    }

    // ==========================================
    // 4. Product Management & Approval Workflow (Member 5)
    // ==========================================

    @GetMapping("/products")
    @Operation(summary = "List all Products for Admin",
            description = "Returns all products with optional filters for approval status (PENDING, APPROVED, REJECTED), active state, category, seller, and keyword search.")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAdminProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<ProductDto> products = productService.getProductsForAdmin(
                query, categoryId, subcategoryId, brandId, sellerId, status, isActive, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PatchMapping("/products/{id}/approve")
    @Operation(summary = "Approve Product Catalog Entry (Member 5)", description = "Sets product status to APPROVED so it appears in buyer search and catalog.")
    public ResponseEntity<ApiResponse<ProductDto>> approveProduct(@PathVariable Long id) {
        ProductDto updated = productService.approveProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product approved successfully", updated));
    }

    @PatchMapping("/products/{id}/reject")
    @Operation(summary = "Reject Product Catalog Entry (Member 5)", description = "Rejects a product submission.")
    public ResponseEntity<ApiResponse<ProductDto>> rejectProduct(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.getOrDefault("rejectionReason", "Product specifications do not meet guidelines.") : "Product rejected.";
        ProductDto updated = productService.rejectProduct(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Product rejected", updated));
    }

    @PatchMapping("/products/{id}/status")
    @Operation(summary = "Activate / Deactivate Product (Member 5)", description = "Toggles product active status.")
    public ResponseEntity<ApiResponse<ProductDto>> toggleProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        ProductDto updated = productService.toggleProductActive(id, active);
        return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", updated));
    }
}
