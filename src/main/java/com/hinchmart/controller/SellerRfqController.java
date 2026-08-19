package com.hinchmart.controller;

import com.hinchmart.dto.request.CreateRfqQuoteRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.RfqDto;
import com.hinchmart.dto.response.RfqQuoteDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.RfqQuoteService;
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

@RestController
@RequestMapping("/api/seller/rfqs")
@Tag(name = "Seller RFQ Quotations (Member 2)", description = "Endpoints for Sellers to browse marketplace RFQs and submit quotation bids")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
public class SellerRfqController {

    private final RfqQuoteService rfqQuoteService;
    private final AuthService authService;

    public SellerRfqController(RfqQuoteService rfqQuoteService, AuthService authService) {
        this.rfqQuoteService = rfqQuoteService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "List Open RFQs for Sellers", description = "Returns marketplace RFQs open for seller bidding.")
    public ResponseEntity<ApiResponse<Page<RfqDto>>> getOpenRfqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RfqDto> rfqs = rfqQuoteService.getSellerRfqs(pageable);
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RFQ Details for Seller", description = "Retrieves details and line items of an RFQ for preparing quotation.")
    public ResponseEntity<ApiResponse<RfqDto>> getRfqDetails(Authentication authentication,
                                                             @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqDto rfq = rfqQuoteService.getSellerRfqById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }

    @PostMapping("/{id}/quote")
    @Operation(summary = "Submit RFQ Quotation",
            description = "Submits a seller quotation bid for an RFQ specifying price, GST percentage, delivery charge, and timeline.")
    public ResponseEntity<ApiResponse<RfqQuoteDto>> submitQuote(Authentication authentication,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody CreateRfqQuoteRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqQuoteDto quote = rfqQuoteService.submitQuote(user.getId(), id, request);
        return new ResponseEntity<>(ApiResponse.success("Quotation submitted successfully", quote), HttpStatus.CREATED);
    }
}
