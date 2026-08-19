package com.hinchmart.controller;

import com.hinchmart.dto.request.PaymentCreateRequest;
import com.hinchmart.dto.request.PaymentVerifyRequest;
import com.hinchmart.dto.request.RefundRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.PaymentDto;
import com.hinchmart.dto.response.RefundDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment & Refund Operations (Member 1)", description = "Endpoints for Payment Initiation, Signature Verification, Gateway Transactions, and Refunds")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthService authService;

    public PaymentController(PaymentService paymentService, AuthService authService) {
        this.paymentService = paymentService;
        this.authService = authService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create Payment Order",
            description = "Initializes payment. Automatically recalculates and enforces the exact order total from the database, ignoring client-sent amounts.")
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(Authentication authentication,
                                                                 @Valid @RequestBody PaymentCreateRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        PaymentDto payment = paymentService.createPayment(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Payment initiated successfully", payment), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Verify Payment & Confirm Order",
            description = "Validates payment signature/gateway response, transitions Order to PAID & CONFIRMED, generates GST Invoice, and dispatches notifications.")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(Authentication authentication,
                                                                 @Valid @RequestBody PaymentVerifyRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        PaymentDto verified = paymentService.verifyPayment(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", verified));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Payment Details by Order ID", description = "Returns payment record, transactions, and refund status for an order.")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(Authentication authentication,
                                                                       @PathVariable Long orderId) {
        User user = authService.getCurrentUser(authentication.getName());
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Process Payment Refund", description = "Issues a full or partial refund for a successful payment transaction.")
    public ResponseEntity<ApiResponse<RefundDto>> processRefund(Authentication authentication,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody RefundRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        RefundDto refund = paymentService.processRefund(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", refund));
    }
}
