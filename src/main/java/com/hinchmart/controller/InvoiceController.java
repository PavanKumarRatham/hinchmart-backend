package com.hinchmart.controller;

import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.InvoiceDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "GST Invoice Management (Member 1)", description = "Endpoints for B2B GST Tax Invoice Generation, CGST/SGST/IGST breakdowns, and Tax Compliance")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AuthService authService;

    public InvoiceController(InvoiceService invoiceService, AuthService authService) {
        this.invoiceService = invoiceService;
        this.authService = authService;
    }

    @GetMapping("/orders/{id}/invoice")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get GST Tax Invoice for Order",
            description = "Retrieves the complete GST tax invoice for an order including HSN codes, quantity, CGST/SGST (intra-state) or IGST (inter-state) breakdown, and seller/buyer GSTINs.")
    public ResponseEntity<ApiResponse<InvoiceDto>> getOrderInvoice(Authentication authentication,
                                                                   @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        InvoiceDto invoice = invoiceService.getInvoiceByOrderId(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @PostMapping("/admin/orders/{id}/generate-invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generate / Regenerate GST Invoice (Admin)",
            description = "Admin manual trigger to generate or regenerate the GST Tax Invoice for an order.")
    public ResponseEntity<ApiResponse<InvoiceDto>> generateInvoiceAdmin(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.generateInvoiceForOrder(id);
        return new ResponseEntity<>(ApiResponse.success("Invoice generated successfully", invoice), HttpStatus.CREATED);
    }
}
