package com.hinchmart.controller;

import com.hinchmart.dto.request.RfqCreateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.RfqDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.RfqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
@Tag(name = "RFQ (Request for Quotation)", description = "Endpoints for Buyer RFQ submissions, quotes, and tracking")
@SecurityRequirement(name = "Bearer Authentication")
public class RfqController {

    private final RfqService rfqService;
    private final AuthService authService;

    public RfqController(RfqService rfqService, AuthService authService) {
        this.rfqService = rfqService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Submit a new RFQ (Request for Quotation)",
            description = "Creates a new RFQ with multiple line items, required quantities, units, and delivery requirements.")
    public ResponseEntity<ApiResponse<RfqDto>> createRfq(Authentication authentication,
                                                         @Valid @RequestBody RfqCreateRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqDto created = rfqService.createRfq(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("RFQ created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get My RFQs", description = "Retrieves all RFQs submitted by the currently logged-in buyer.")
    public ResponseEntity<ApiResponse<List<RfqDto>>> getMyRfqs(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        List<RfqDto> rfqs = rfqService.getMyRfqs(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RFQ Details by ID", description = "Returns full details and line items for a specific RFQ.")
    public ResponseEntity<ApiResponse<RfqDto>> getRfqById(Authentication authentication,
                                                          @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqDto rfq = rfqService.getRfqById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List all Marketplace RFQs (Seller / Admin)", description = "Returns all marketplace RFQs with pagination for sellers to bid/quote.")
    public ResponseEntity<ApiResponse<Page<RfqDto>>> getAllRfqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RfqDto> rfqs = rfqService.getAllRfqs(pageable);
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }
}
