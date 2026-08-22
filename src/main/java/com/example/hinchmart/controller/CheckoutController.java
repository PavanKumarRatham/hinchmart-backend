package com.example.hinchmart.controller;

import com.example.hinchmart.dto.CheckoutPreviewRequest;
import com.example.hinchmart.dto.CheckoutPreviewResponse;
import com.example.hinchmart.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/checkout/preview
     * Calculate checkout preview with subtotal, GST, delivery charge, and grand total.
     */
    @PostMapping("/preview")
    public ResponseEntity<CheckoutPreviewResponse> getCheckoutPreview(
            @RequestBody(required = false) CheckoutPreviewRequest request) {
        CheckoutPreviewResponse preview = orderService.getCheckoutPreview(request);
        return ResponseEntity.ok(preview);
    }
}
