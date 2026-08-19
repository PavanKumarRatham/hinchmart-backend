package com.hinchmart.controller;

import com.hinchmart.dto.request.CheckoutPreviewRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.CheckoutPreviewDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@Tag(name = "Checkout Operations (Member 2)", description = "Endpoints for Order Preview, Subtotal, GST (18%), Delivery Charges and Grand Total Calculation")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
public class CheckoutController {

    private final OrderService orderService;
    private final AuthService authService;

    public CheckoutController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview Checkout Breakdown",
            description = "Calculates final order costs including bulk price discounts, GST (18%), delivery transport charges, and grand total.")
    public ResponseEntity<ApiResponse<CheckoutPreviewDto>> previewCheckout(
            Authentication authentication,
            @RequestBody(required = false) CheckoutPreviewRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        CheckoutPreviewDto preview = orderService.previewCheckout(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }
}
