package com.hinchmart.controller;

import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.RfqQuoteDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.RfqQuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer")
@Tag(name = "Buyer RFQ Quotes & Acceptance (Member 2)", description = "Endpoints for Buyers to compare quotes submitted by multiple sellers and accept/reject them")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
public class BuyerRfqQuoteController {

    private final RfqQuoteService rfqQuoteService;
    private final AuthService authService;

    public BuyerRfqQuoteController(RfqQuoteService rfqQuoteService, AuthService authService) {
        this.rfqQuoteService = rfqQuoteService;
        this.authService = authService;
    }

    @GetMapping("/rfqs/{id}/quotes")
    @Operation(summary = "List Seller Quotes for Buyer RFQ",
            description = "Returns all quotations submitted by various sellers (e.g. Seller A ₹58,500, Seller B ₹59,200, Seller C ₹58,000) for comparison.")
    public ResponseEntity<ApiResponse<List<RfqQuoteDto>>> getQuotesForRfq(
            Authentication authentication,
            @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        List<RfqQuoteDto> quotes = rfqQuoteService.getBuyerQuotesForRfq(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @PostMapping("/quotes/{id}/accept")
    @Operation(summary = "Accept Seller RFQ Quotation",
            description = "Accepts a chosen seller quotation, rejects competing quotes on that RFQ, and closes the RFQ.")
    public ResponseEntity<ApiResponse<RfqQuoteDto>> acceptQuote(
            Authentication authentication,
            @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqQuoteDto accepted = rfqQuoteService.acceptQuote(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Quotation accepted successfully", accepted));
    }

    @PostMapping("/quotes/{id}/reject")
    @Operation(summary = "Reject Seller RFQ Quotation", description = "Rejects a specific seller quotation.")
    public ResponseEntity<ApiResponse<RfqQuoteDto>> rejectQuote(
            Authentication authentication,
            @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        RfqQuoteDto rejected = rfqQuoteService.rejectQuote(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Quotation rejected", rejected));
    }
}
