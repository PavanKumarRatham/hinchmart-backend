package com.hinchmart.controller;

import com.hinchmart.dto.request.RfqCreateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.RfqDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.RfqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
@Tag(name = "RFQ (Request for Quotation)", description = "Endpoints for Buyer RFQ submissions, quotes, and tracking")
public class RfqController {

    private final RfqService rfqService;
    private final AuthService authService;

    public RfqController(RfqService rfqService, AuthService authService) {
        this.rfqService = rfqService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Submit a new RFQ (Request for Quotation)",
            description = "Creates a new RFQ with multiple line items, required quantities, units, and delivery requirements.")
    public ResponseEntity<ApiResponse<RfqDto>> createRfq(@RequestParam Long userId,
                                                         @Valid @RequestBody RfqCreateRequest request) {
        User user = authService.getUserById(userId);
        RfqDto created = rfqService.createRfq(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("RFQ created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "Get Buyer RFQs", description = "Retrieves all RFQs submitted by the specified buyer.")
    public ResponseEntity<ApiResponse<List<RfqDto>>> getMyRfqs(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        List<RfqDto> rfqs = rfqService.getMyRfqs(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RFQ Details by ID", description = "Returns full details and line items for a specific RFQ.")
    public ResponseEntity<ApiResponse<RfqDto>> getRfqById(@RequestParam Long userId,
                                                          @PathVariable Long id) {
        User user = authService.getUserById(userId);
        RfqDto rfq = rfqService.getRfqById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }

    @GetMapping
    @Operation(summary = "List all Marketplace RFQs (Seller / Admin)", description = "Returns all marketplace RFQs with pagination for sellers to bid/quote.")
    public ResponseEntity<ApiResponse<Page<RfqDto>>> getAllRfqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RfqDto> rfqs = rfqService.getAllRfqs(pageable);
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }
}
